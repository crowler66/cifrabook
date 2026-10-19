
package com.example.cifrabook

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var scrollViewPdf: ScrollView
    private lateinit var pdfPagesContainer: LinearLayout

    private lateinit var floatingScrollMenu: View
    private lateinit var btnDotPoint: View
    private lateinit var expandedClockLayout: View

    private lateinit var btnPlayPauseCenter: Button
    private lateinit var btnSpeedMinus: Button
    private lateinit var btnSpeedPlus: Button

    private lateinit var pdfProgressTrack: View
    private lateinit var pdfProgressFill: View

    private var liquidRingView: View? = null

    private var isScrolling = false
    private var isMenuExpanded = false

    private val scrollHandler = Handler(Looper.getMainLooper())

    private val menuTimeout = 3000L

    // Máximo de páginas unidas em cada imagem.
    private val pagesPerImage = 12

    // Intervalo menor = rolagem mais rápida.
    private var scrollDelay = 50L
    private val minDelay = 1L
    private val maxDelay = 55L

    private val hideMenuRunnable = Runnable {
        if (!isFinishing && !isDestroyed && isMenuExpanded) {
            closeFloatingMenu()
        }
    }

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (!isScrolling || isFinishing || isDestroyed) return

            scrollViewPdf.smoothScrollBy(0, 1)
            scrollHandler.postDelayed(this, scrollDelay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        scrollViewPdf = findViewById(R.id.scrollViewPdf)
        pdfPagesContainer = findViewById(R.id.pdfPagesContainer)

        floatingScrollMenu = findViewById(R.id.floatingScrollMenu)
        btnDotPoint = findViewById(R.id.btnDotPoint)
        expandedClockLayout = findViewById(R.id.expandedClockLayout)

        btnPlayPauseCenter = findViewById(R.id.btnPlayPauseCenter)
        btnSpeedMinus = findViewById(R.id.btnSpeedMinus)
        btnSpeedPlus = findViewById(R.id.btnSpeedPlus)

        liquidRingView = findViewById(R.id.liquidRingView)

        pdfProgressTrack = findViewById(R.id.pdfProgressTrack)
        pdfProgressFill = findViewById(R.id.pdfProgressFill)

        // Atualiza a linha vermelha durante a rolagem.
        scrollViewPdf.setOnScrollChangeListener(
            View.OnScrollChangeListener { _, _, _, _, _ ->
                atualizarProgressoPdf()
            }
        )

        setupFloatingMenuBehavior()
        resetRingProgress()

        val pdfUriString = intent.getStringExtra("PDF_URI")

        if (pdfUriString.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Nenhum PDF foi selecionado.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val uri = Uri.parse(pdfUriString)

        val canRead = try {
            contentResolver.openFileDescriptor(uri, "r")?.use {
                true
            } ?: false
        } catch (e: Exception) {
            false
        }

        if (!canRead) {
            Toast.makeText(
                this,
                "Não foi possível acessar esse PDF.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        RecentManager.addRecent(this, pdfUriString)

        // Carrega as imagens do PDF fora da thread principal.
        lifecycleScope.launch {
            val groupedBitmaps = try {
                withContext(Dispatchers.IO) {
                    renderPdfToGroupedBitmaps(uri)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (isFinishing || isDestroyed) {
                groupedBitmaps?.forEach { bitmap ->
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
                return@launch
            }

            if (groupedBitmaps.isNullOrEmpty()) {
                Toast.makeText(
                    this@PdfViewerActivity,
                    "Erro ao carregar o PDF.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            pdfPagesContainer.removeAllViews()

            groupedBitmaps.forEach { bitmap ->

                // PhotoView permite ampliar e reduzir com os dedos.
                val imageView = PhotoView(this@PdfViewerActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER

                    setPadding(0, 0, 0, 0)

                    contentDescription = "Grupo de páginas do PDF"

                    setImageBitmap(bitmap)
                }

                pdfPagesContainer.addView(imageView)
            }

            pdfPagesContainer.post {
                atualizarProgressoPdf()
            }
        }

        // Play/Pause original: mantido sem alterar o visual.
        btnPlayPauseCenter.setOnClickListener {
            isScrolling = !isScrolling

            if (isScrolling) {
                btnPlayPauseCenter.text = "❚❚"

                scrollHandler.removeCallbacks(scrollRunnable)
                scrollHandler.post(scrollRunnable)
            } else {
                btnPlayPauseCenter.text = "▶"

                scrollHandler.removeCallbacks(scrollRunnable)
            }

            scheduleMenuHide()
        }

        // Diminuir a velocidade.
        btnSpeedMinus.setOnClickListener {
            scrollDelay = (scrollDelay + 6L).coerceAtMost(maxDelay)

            updateRingProgress()
            scheduleMenuHide()
        }

        // Aumentar a velocidade.
        btnSpeedPlus.setOnClickListener {
            scrollDelay = (scrollDelay - 5L).coerceAtLeast(minDelay)

            updateRingProgress()
            scheduleMenuHide()
        }
    }

    /**
     * Cria uma imagem para cada grupo de até 12 páginas.
     * Exemplo: 30 páginas = grupos de 12, 12 e 6 páginas.
     */
    private fun renderPdfToGroupedBitmaps(uri: Uri): List<Bitmap>? {
        val groupedBitmaps = mutableListOf<Bitmap>()

        try {
            contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->

                    val pageCount = renderer.pageCount

                    if (pageCount <= 0) return null

                    var firstPage = 0

                    while (firstPage < pageCount) {
                        val lastPageExclusive =
                            minOf(firstPage + pagesPerImage, pageCount)

                        val pageBitmaps = mutableListOf<Bitmap>()

                        try {
                            for (pageIndex in firstPage until lastPageExclusive) {
                                val page = renderer.openPage(pageIndex)

                                try {
                                    val pageBitmap = Bitmap.createBitmap(
                                        page.width * 2,
                                        page.height * 2,
                                        Bitmap.Config.ARGB_8888
                                    )

                                    try {
                                        page.render(
                                            pageBitmap,
                                            null,
                                            null,
                                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                        )

                                        pageBitmaps.add(pageBitmap)
                                    } catch (e: Exception) {
                                        pageBitmap.recycle()
                                        throw e
                                    }
                                } finally {
                                    page.close()
                                }
                            }

                            val groupedBitmap =
                                combinePageBitmaps(pageBitmaps)

                            groupedBitmaps.add(groupedBitmap)
                        } finally {
                            pageBitmaps.forEach { bitmap ->
                                if (!bitmap.isRecycled) {
                                    bitmap.recycle()
                                }
                            }
                        }

                        firstPage = lastPageExclusive
                    }
                }
            }

            return groupedBitmaps
        } catch (e: CancellationException) {
            groupedBitmaps.forEach { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            throw e
        } catch (e: Exception) {
            e.printStackTrace()

            groupedBitmaps.forEach { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }

            return null
        }
    }

    /**
     * Une verticalmente as páginas de um grupo.
     */
    private fun combinePageBitmaps(
        pageBitmaps: List<Bitmap>
    ): Bitmap {
        require(pageBitmaps.isNotEmpty()) {
            "O grupo não contém páginas."
        }

        var totalWidth = 0
        var totalHeight = 0

        for (bitmap in pageBitmaps) {
            totalWidth = maxOf(totalWidth, bitmap.width)
            totalHeight += bitmap.height
        }

        val combinedBitmap = Bitmap.createBitmap(
            totalWidth,
            totalHeight,
            Bitmap.Config.ARGB_8888
        )

        try {
            val canvas = Canvas(combinedBitmap)
            var currentY = 0f

            for (bitmap in pageBitmaps) {
                canvas.drawBitmap(
                    bitmap,
                    0f,
                    currentY,
                    null
                )

                currentY += bitmap.height.toFloat()
            }

            return combinedBitmap
        } catch (e: Exception) {
            combinedBitmap.recycle()
            throw e
        }
    }

    // Atualiza a linha vermelha conforme a posição do PDF.
    private fun atualizarProgressoPdf() {
        if (
            scrollViewPdf.height <= 0 ||
            pdfProgressTrack.width <= 0
        ) {
            return
        }

        val conteudo = scrollViewPdf.getChildAt(0) ?: return

        val alturaRolavel = conteudo.height - scrollViewPdf.height

        val progresso = if (alturaRolavel > 0) {
            (scrollViewPdf.scrollY.toFloat() / alturaRolavel)
                .coerceIn(0f, 1f)
        } else {
            0f
        }

        val larguraPreenchida =
            (pdfProgressTrack.width * progresso).toInt()

        val params = pdfProgressFill.layoutParams

        if (params.width != larguraPreenchida) {
            params.width = larguraPreenchida
            pdfProgressFill.layoutParams = params
        }
    }

    // Zera o indicador de velocidade ao abrir o leitor.
    private fun resetRingProgress() {
        (liquidRingView as? SpeedRingView)?.setProgress(0f, 1f)
    }

    // Atualiza o anel conforme a velocidade escolhida.
    private fun updateRingProgress() {
        val currentSpeedStep = (maxDelay - scrollDelay).toFloat()
        val maxSpeedStep = (maxDelay - minDelay).toFloat()

        (liquidRingView as? SpeedRingView)?.setProgress(
            currentSpeedStep,
            maxSpeedStep
        )
    }

    private fun setupFloatingMenuBehavior() {
        var dX = 0f
        var dY = 0f

        floatingScrollMenu.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    view.performClick()
                    true
                }

                else -> false
            }
        }

        btnDotPoint.setOnClickListener {
            openFloatingMenu()
        }

        expandedClockLayout.setOnClickListener {
            scheduleMenuHide()
        }
    }

    // Abre o menu sem zerar o indicador de velocidade.
    private fun openFloatingMenu() {
        scrollHandler.removeCallbacks(hideMenuRunnable)

        isMenuExpanded = true

        btnDotPoint.visibility = View.GONE
        expandedClockLayout.visibility = View.VISIBLE

        scheduleMenuHide()
    }

    private fun scheduleMenuHide() {
        scrollHandler.removeCallbacks(hideMenuRunnable)

        if (isMenuExpanded) {
            scrollHandler.postDelayed(
                hideMenuRunnable,
                menuTimeout
            )
        }
    }

    private fun closeFloatingMenu() {
        scrollHandler.removeCallbacks(hideMenuRunnable)

        isMenuExpanded = false

        expandedClockLayout.visibility = View.GONE
        btnDotPoint.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        isScrolling = false

        scrollHandler.removeCallbacks(scrollRunnable)
        scrollHandler.removeCallbacks(hideMenuRunnable)

        super.onDestroy()
    }
}
