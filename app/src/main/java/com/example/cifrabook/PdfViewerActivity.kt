package com.example.cifrabook

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var scrollViewPdf: ScrollView
    private lateinit var ivPdfFull: PhotoView
    private lateinit var btnToggleScroll: Button
    private lateinit var seekBarSpeed: SeekBar

    private var isScrolling = false
    private val scrollHandler = Handler(Looper.getMainLooper())

    private var scrollDelay = 35L

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isScrolling) {
                scrollViewPdf.smoothScrollBy(0, 1)
                scrollHandler.postDelayed(this, scrollDelay)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        scrollViewPdf = findViewById(R.id.scrollViewPdf)
        ivPdfFull = findViewById(R.id.ivPdfFull)
        btnToggleScroll = findViewById(R.id.btnToggleScroll)
        seekBarSpeed = findViewById(R.id.seekBarSpeed)

        val pdfUriString = intent.getStringExtra("PDF_URI")
        if (pdfUriString != null) {
            val uri = Uri.parse(pdfUriString)

            // Segurança: Valida se o arquivo pode ser lido antes de prosseguir (evita crash pós-reinstalação)
            val canRead = try {
                contentResolver.openFileDescriptor(uri, "r")?.close()
                true
            } catch (e: Exception) {
                false
            }

            if (!canRead) {
                finish()
                return
            }

            RecentManager.addRecent(this, pdfUriString)

            btnToggleScroll.isEnabled = false
            btnToggleScroll.text = "A carregar cifra..."

            lifecycleScope.launch {
                val combinedBitmap = withContext(Dispatchers.IO) {
                    renderPdfToOneBitmap(uri)
                }

                if (combinedBitmap != null) {
                    ivPdfFull.setImageBitmap(combinedBitmap)
                }

                btnToggleScroll.isEnabled = true
                btnToggleScroll.text = "Iniciar Auto-Scroll"
            }
        }

        btnToggleScroll.setOnClickListener {
            isScrolling = !isScrolling
            if (isScrolling) {
                btnToggleScroll.text = "Parar Auto-Scroll"
                scrollHandler.post(scrollRunnable)
            } else {
                btnToggleScroll.text = "Iniciar Auto-Scroll"
                scrollHandler.removeCallbacks(scrollRunnable)
            }
        }

        // Barra de velocidade com início lento preservado e máximo estendido
        seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val safeProgress = progress.coerceIn(1, 10)
                scrollDelay = (95 - (safeProgress * 9)).coerceAtLeast(8).toLong()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun renderPdfToOneBitmap(uri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r") ?: return null
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val pageCount = pdfRenderer.pageCount

            val bitmaps = mutableListOf<Bitmap>()
            var totalWidth = 0
            var totalHeight = 0

            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                // Mantém a proporção original de alta qualidade para o zoom da PhotoView
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)

                totalWidth = maxOf(totalWidth, bitmap.width)
                totalHeight += bitmap.height
                page.close()
            }

            val combinedBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(combinedBitmap)
            var currentY = 0f

            for (bmp in bitmaps) {
                canvas.drawBitmap(bmp, 0f, currentY, null)
                currentY += bmp.height.toFloat()
                bmp.recycle()
            }

            pdfRenderer.close()
            parcelFileDescriptor.close()
            return combinedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollHandler.removeCallbacks(scrollRunnable)
    }
}