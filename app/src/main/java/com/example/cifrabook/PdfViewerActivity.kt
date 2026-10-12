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
import com.github.chrisbanes.photoview.PhotoView

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var scrollViewPdf: ScrollView
    private lateinit var ivPdfFull: PhotoView
    private lateinit var btnToggleScroll: Button
    private lateinit var seekBarSpeed: SeekBar

    private var isScrolling = false
    private val scrollHandler = Handler(Looper.getMainLooper())
    private var scrollSpeed = 2L

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isScrolling) {
                // Rola o ScrollView verticalmente de forma contínua
                scrollViewPdf.smoothScrollBy(0, scrollSpeed.toInt())
                scrollHandler.postDelayed(this, 30)
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

        // Recebe o URI da cifra e renderiza todas as páginas num único Bitmap
        val pdfUriString = intent.getStringExtra("PDF_URI")
        if (pdfUriString != null) {
            RecentManager.addRecent(this, pdfUriString)
            val uri = Uri.parse(pdfUriString)
            val combinedBitmap = renderPdfToOneBitmap(uri)
            if (combinedBitmap != null) {
                ivPdfFull.setImageBitmap(combinedBitmap)
            }
        }

        // Configurar o Botão de Play/Pause do Auto-Scroll
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

        // Configurar a barra de velocidade de rolagem
        seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                scrollSpeed = progress.toLong().coerceAtLeast(1L)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // Função que converte todas as páginas do PDF e une-as verticalmente numa só imagem
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
                // Multiplicador 2x para garantir excelente nitidez ao aplicar o zoom
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)

                totalWidth = maxOf(totalWidth, bitmap.width)
                totalHeight += bitmap.height
                page.close()
            }

            // Cria o bitmap unificado (todas as páginas empilhadas verticalmente)
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