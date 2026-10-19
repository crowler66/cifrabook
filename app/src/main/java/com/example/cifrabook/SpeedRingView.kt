
package com.example.cifrabook

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SpeedRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    // Círculo de fundo.
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33FFFFFF
        style = Paint.Style.STROKE
        strokeWidth = 6f * density
    }

    // Linha vermelha de progresso.
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF3B30.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 8f * density
        strokeCap = Paint.Cap.ROUND
    }

    private val rectF = RectF()

    var progressPercent: Float = 0f
        private set

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val diameter = minOf(width, height).toFloat()
        val margin = progressPaint.strokeWidth / 2f + 2f * density

        rectF.set(
            (width - diameter) / 2f + margin,
            (height - diameter) / 2f + margin,
            (width + diameter) / 2f - margin,
            (height + diameter) / 2f - margin
        )

        // Desenha o círculo de fundo.
        canvas.drawOval(rectF, backgroundPaint)

        // Começa às 12 horas e cresce no sentido anti-horário.
        val sweepAngle = -progressPercent.coerceIn(0f, 1f) * 360f

        if (sweepAngle != 0f) {
            canvas.drawArc(
                rectF,
                -90f,
                sweepAngle,
                false,
                progressPaint
            )
        }
    }

    fun setProgress(current: Float, max: Float) {
        progressPercent = if (max > 0f) {
            (current / max).coerceIn(0f, 1f)
        } else {
            0f
        }

        invalidate()
    }
}
