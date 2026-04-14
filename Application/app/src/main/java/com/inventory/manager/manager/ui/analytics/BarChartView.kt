package com.inventory.manager.ui.analytics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.inventory.manager.data.model.SalesTrend
import kotlin.math.max

class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var data: List<SalesTrend> = emptyList()
    private val MAX_BARS = 15
    private val PADDING = 40f
    private val LABEL_H = 38f

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888"); textSize = 24f; textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#444444"); textSize = 22f; textAlign = Paint.Align.CENTER
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EEEEEE"); strokeWidth = 2f; style = Paint.Style.STROKE
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AAAAAA"); textSize = 36f; textAlign = Paint.Align.CENTER
    }

    fun setData(trends: List<SalesTrend>) {
        data = if (trends.size > MAX_BARS) trends.takeLast(MAX_BARS) else trends
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty() || data.all { it.totalUnits == 0 }) {
            canvas.drawText("No data yet", width / 2f, height / 2f, emptyPaint); return
        }
        val w = width.toFloat(); val h = height.toFloat()
        val chartL = PADDING; val chartR = w - PADDING
        val chartB = h - LABEL_H - 4f; val chartT = PADDING
        val maxUnits = data.maxOf { it.totalUnits }.coerceAtLeast(1)
        val barSlot = (chartR - chartL) / data.size
        val barW = barSlot * 0.55f
        val gap = (barSlot - barW) / 2f

        listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { frac ->
            val y = chartB - frac * (chartB - chartT)
            canvas.drawLine(chartL, y, chartR, y, gridPaint)
        }

        data.forEachIndexed { i, trend ->
            val frac = trend.totalUnits.toFloat() / maxUnits
            val barH = max(frac * (chartB - chartT), if (frac > 0) 4f else 0f)
            val left = chartL + i * barSlot + gap
            val right = left + barW
            val top = chartB - barH

            barPaint.shader = LinearGradient(left, top, left, chartB,
                Color.parseColor("#9C4DCC"), Color.parseColor("#6200EE"), Shader.TileMode.CLAMP)
            if (frac > 0) canvas.drawRoundRect(left, top, right, chartB, 6f, 6f, barPaint)

            canvas.drawText(trend.label, left + barW / 2, chartB + LABEL_H * 0.85f, labelPaint)
            if (frac > 0.08f)
                canvas.drawText(if (trend.totalUnits >= 1000) "${trend.totalUnits / 1000}k"
                    else trend.totalUnits.toString(),
                    left + barW / 2, max(top - 6f, chartT + 22f), valuePaint)
        }
    }
}
