
package com.example.cifrabook

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.BlurMaskFilter
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class TunerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val purple = Color.rgb(180, 40, 255)
    private val brightPurple = Color.rgb(222, 120, 255)
    private val palePurple = Color.rgb(235, 190, 255)
    private val green = Color.rgb(50, 255, 125)
    private val white = Color.WHITE
    private val muted = Color.rgb(205, 190, 218)

    // Da corda mais grave para a mais aguda.
    private val notes = arrayOf("E", "A", "D", "G", "B", "E")
    private val noteNames = arrayOf("Mi", "Lá", "Ré", "Sol", "Si", "Mi")
    private val freqs = floatArrayOf(
        82.41f, 110.00f, 146.83f,
        196.00f, 246.94f, 329.63f
    )

    var currentPitch: Float = 0f
        set(value) {
            field = if (value.isFinite() && value in 60f..550f) value else 0f

            if (field > 0f) {
                selectedIndex = freqs.indices.minByOrNull { index ->
                    abs(centsFrom(freqs[index], field))
                } ?: selectedIndex
            }

            invalidate()
        }

    var selectedIndex: Int = 2
        private set

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val cx = w / 2f
        val cy = h * 0.405f
        val radius = min(w * 0.43f, h * 0.365f)
            .coerceAtLeast(1f)

        val tuned = isTuned()
        val activeColor = if (tuned) green else purple

        // Fundo circular discreto.
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(28, 12, 43)
        canvas.drawCircle(cx, cy, radius * 0.96f, paint)

        // Brilho externo do mostrador.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.065f
        paint.color = activeColor
        paint.alpha = 65
        paint.maskFilter = BlurMaskFilter(
            radius * 0.07f,
            BlurMaskFilter.Blur.NORMAL
        )
        canvas.drawCircle(cx, cy, radius, paint)
        paint.maskFilter = null
        paint.alpha = 255

        // Aro externo duplo.
        paint.strokeWidth = radius * 0.025f
        paint.color = Color.rgb(153, 132, 177)
        canvas.drawCircle(cx, cy, radius, paint)

        paint.strokeWidth = radius * 0.009f
        paint.color = Color.rgb(86, 61, 112)
        canvas.drawCircle(cx, cy, radius * 0.965f, paint)

        // Segmentos luminosos ao redor do círculo.
        val arcRect = RectF(
            cx - radius * 0.88f,
            cy - radius * 0.88f,
            cx + radius * 0.88f,
            cy + radius * 0.88f
        )

        val segmentCount = 36
        val segmentSweep = 7.0f
        val segmentStep = 270f / segmentCount

        for (i in 0 until segmentCount) {
            val start = 135f + i * segmentStep
            val color = when {
                i in 9..17 -> green
                else -> purple
            }

            paint.color = color
            paint.alpha = if (tuned) 255 else 225
            paint.strokeWidth = radius * 0.045f
            paint.style = Paint.Style.STROKE
            paint.maskFilter = BlurMaskFilter(
                radius * 0.018f,
                BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawArc(
                arcRect,
                start,
                segmentSweep,
                false,
                paint
            )
            paint.maskFilter = null
            paint.alpha = 255
        }

        // Anel interno fino.
        paint.color = brightPurple
        paint.alpha = 175
        paint.strokeWidth = radius * 0.008f
        canvas.drawCircle(cx, cy, radius * 0.77f, paint)
        paint.alpha = 255

        // Graduações do relógio.
        for (i in 0 until 60) {
            val angle = Math.toRadians(i * 6.0 - 90.0)
            val major = i % 5 == 0

            val outer = radius * 0.75f
            val inner = radius * if (major) 0.65f else 0.705f

            val sx = cx + cos(angle).toFloat() * inner
            val sy = cy + sin(angle).toFloat() * inner
            val ex = cx + cos(angle).toFloat() * outer
            val ey = cy + sin(angle).toFloat() * outer

            paint.color = if (major) brightPurple else palePurple
            paint.alpha = if (major) 245 else 145
            paint.strokeWidth = radius * if (major) 0.014f else 0.006f

            canvas.drawLine(sx, sy, ex, ey, paint)
        }
        paint.alpha = 255

        // Nota musical no topo do mostrador.
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = radius * 0.12f
        paint.color = if (tuned) green else brightPurple
        paint.maskFilter = BlurMaskFilter(
            radius * 0.015f,
            BlurMaskFilter.Blur.NORMAL
        )
        canvas.drawText("♫", cx, cy - radius * 0.40f, paint)
        paint.maskFilter = null

        // Estado da afinação.
        val status = when {
            currentPitch <= 0f -> "TOQUE UMA CORDA"
            tuned -> "AFINADO!"
            else -> "AJUSTANDO..."
        }

        paint.textSize = min(radius * 0.105f, w * 0.057f)
        paint.color = if (tuned) green else white
        paint.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
        )
        canvas.drawText(status, cx, cy - radius * 0.24f, paint)

        // Indicadores GRAVE e AGUDO em cápsulas roxas.
        val pillY = cy
        val pillW = radius * 0.40f
        val pillH = radius * 0.13f

        drawPill(
            canvas,
            cx - radius * 0.55f,
            pillY,
            pillW,
            pillH,
            "GRAVE"
        )

        drawPill(
            canvas,
            cx + radius * 0.55f,
            pillY,
            pillW,
            pillH,
            "AGUDO"
        )

        // Ponteiro: desloca-se de acordo com a diferença em cents.
        val target = freqs[selectedIndex]
        val cents = if (currentPitch > 0f) {
            centsFrom(target, currentPitch)
        } else {
            0f
        }

        val angle = Math.toRadians(
            -90.0 + cents.coerceIn(-50f, 50f) * 1.25
        )

        val needleLength = radius * 0.43f
        val tipX = cx + cos(angle).toFloat() * needleLength
        val tipY = cy + sin(angle).toFloat() * needleLength

        val dx = tipX - cx
        val dy = tipY - cy
        val length = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val px = -dy / length * radius * 0.026f
        val py = dx / length * radius * 0.026f

        val needle = Path().apply {
            moveTo(cx + px, cy + py)
            lineTo(tipX, tipY)
            lineTo(cx - px, cy - py)
            close()
        }

        // Halo da seta.
        paint.style = Paint.Style.FILL
        paint.color = activeColor
        paint.maskFilter = BlurMaskFilter(
            radius * 0.045f,
            BlurMaskFilter.Blur.NORMAL
        )
        canvas.drawPath(needle, paint)
        paint.maskFilter = null

        // Corpo da seta com brilho.
        paint.color = if (tuned) green else brightPurple
        canvas.drawPath(needle, paint)

        // Núcleo da seta.
        paint.color = Color.rgb(55, 38, 72)
        canvas.drawCircle(cx, cy, radius * 0.055f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = if (tuned) green else palePurple
        paint.strokeWidth = radius * 0.013f
        canvas.drawCircle(cx, cy, radius * 0.055f, paint)
        paint.style = Paint.Style.FILL

        // Mensagem abaixo do ponteiro.
        paint.textSize = min(radius * 0.075f, w * 0.042f)
        paint.color = if (tuned) green else white
        paint.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
        )

        val lowerText = when {
            currentPitch <= 0f -> "Aguardando som"
            tuned -> "Afinado"
            cents < 0f -> "Ajuste a tensão da corda"
            else -> "Ajuste a tensão da corda"
        }

        canvas.drawText(
            lowerText,
            cx,
            cy + radius * 0.48f,
            paint
        )

        // Frequência detectada.
        if (currentPitch > 0f) {
            paint.textSize = radius * 0.052f
            paint.color = palePurple
            paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            canvas.drawText(
                String.format(
                    java.util.Locale.US,
                    "%.1f Hz",
                    currentPitch
                ),
                cx,
                cy + radius * 0.59f,
                paint
            )
        }

        // Notas distribuídas em arco, acompanhando o círculo.
        drawNotesOnArc(canvas, cx, cy, radius, w, h)

// Assinatura Cr66 roxa, sólida e sem brilho.
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        paint.textSize = 13f * resources.displayMetrics.density
        paint.color = Color.rgb(190, 100, 255)
        paint.alpha = 255
        paint.clearShadowLayer()
        paint.maskFilter = null

        canvas.drawText(
            "Cr",
            cx,
            h - 12f * resources.displayMetrics.density,
            paint
        )

        canvas.drawText("Cr", cx, h - 12f * resources.displayMetrics.density, paint)
        paint.maskFilter = null
        paint.alpha = 255
    }

    private fun drawPill(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        pillWidth: Float,
        pillHeight: Float,
        label: String
    ) {
        val rect = RectF(
            centerX - pillWidth / 2f,
            centerY - pillHeight / 2f,
            centerX + pillWidth / 2f,
            centerY + pillHeight / 2f
        )

        // Halo externo.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = purple
        paint.maskFilter = BlurMaskFilter(
            10f,
            BlurMaskFilter.Blur.NORMAL
        )
        canvas.drawRoundRect(
            rect,
            pillHeight / 2f,
            pillHeight / 2f,
            paint
        )
        paint.maskFilter = null

        // Fundo da cápsula.
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(39, 17, 58)
        canvas.drawRoundRect(
            rect,
            pillHeight / 2f,
            pillHeight / 2f,
            paint
        )

        // Contorno.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f
        paint.color = brightPurple
        canvas.drawRoundRect(
            rect,
            pillHeight / 2f,
            pillHeight / 2f,
            paint
        )

        // Texto.
        paint.style = Paint.Style.FILL
        paint.color = palePurple
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = pillHeight * 0.42f
        paint.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
        )

        canvas.drawText(
            label,
            centerX,
            centerY - (paint.ascent() + paint.descent()) / 2f,
            paint
        )
    }

    private fun drawNotesOnArc(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        w: Float,
        h: Float
    ) {
        // As notas seguem o arco inferior do mostrador.
        val noteRadius = radius * 1.10f
        val firstAngle = 145.0
        val lastAngle = 35.0
        val angleStep = (lastAngle - firstAngle) / 5.0

        for (i in notes.indices) {
            val angle = Math.toRadians(firstAngle + i * angleStep)

            val x = cx + cos(angle).toFloat() * noteRadius
            val y = cy + sin(angle).toFloat() * noteRadius

            val selected = i == selectedIndex

            // Tamanho das notas: maior que na versão anterior.
            val nameSize = min(w * 0.050f, 27f)
            val letterSize = min(w * 0.061f, 33f)

            // Nome em português, um pouco menor e mais claro.
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(
                "sans-serif",
                Typeface.NORMAL
            )
            paint.textSize = nameSize
            paint.color = if (selected) palePurple else muted

            canvas.drawText(
                noteNames[i],
                x,
                y + nameSize * 0.25f,
                paint
            )

            // Letra internacional maior e em negrito.
            paint.typeface = Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )
            paint.textSize = letterSize
            paint.color = if (selected) brightPurple else white

            canvas.drawText(
                "(${notes[i]})",
                x,
                y + nameSize * 0.25f + letterSize * 1.65f,
                paint
            )

            if (selected) {
                drawSelectionArrow(
                    canvas,
                    x,
                    y - nameSize * 0.55f,
                    min(w * 0.035f, 19f)
                )
            }
        }
    }

    private fun drawSelectionArrow(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float
    ) {
        // Seta triangular, apontando para a nota selecionada.
        val arrow = Path().apply {
            moveTo(x, y + size)
            lineTo(x - size * 0.90f, y - size * 0.55f)
            lineTo(x + size * 0.90f, y - size * 0.55f)
            close()
        }

        // Brilho externo.
        paint.style = Paint.Style.FILL
        paint.color = purple
        paint.maskFilter = BlurMaskFilter(
            size * 0.85f,
            BlurMaskFilter.Blur.NORMAL
        )
        canvas.drawPath(arrow, paint)
        paint.maskFilter = null

        // Preenchimento em gradiente.
        paint.shader = android.graphics.LinearGradient(
            x,
            y - size,
            x,
            y + size,
            palePurple,
            purple,
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawPath(arrow, paint)
        paint.shader = null

        // Contorno luminoso.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f
        paint.color = palePurple
        canvas.drawPath(arrow, paint)
        paint.style = Paint.Style.FILL
    }

    private fun centsFrom(
        targetFrequency: Float,
        measuredFrequency: Float
    ): Float {
        if (targetFrequency <= 0f || measuredFrequency <= 0f) {
            return 0f
        }

        return (
                1200.0 * ln(measuredFrequency / targetFrequency) /
                        ln(2.0)
                ).toFloat()
    }

    private fun isTuned(): Boolean {
        if (currentPitch <= 0f) return false

        return abs(
            centsFrom(freqs[selectedIndex], currentPitch)
        ) <= 5f
    }
}