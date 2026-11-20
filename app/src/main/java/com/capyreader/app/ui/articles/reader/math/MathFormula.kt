package com.capyreader.app.ui.articles.reader.math

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.jocmp.mallet.tex.PathSink
import com.jocmp.mallet.tex.SvgPath
import com.jocmp.mallet.tex.TexBox
import com.jocmp.mallet.tex.TexColor
import com.jocmp.mallet.tex.TexItem
import com.jocmp.mallet.tex.transform
import java.util.WeakHashMap

/**
 * Draws a typeset formula at [emPx] pixels per em, sized to its box with the baseline
 * [TexBox.height] ems from the top.
 */
@Composable
fun MathFormula(
    box: TexBox,
    emPx: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val size =
        with(LocalDensity.current) {
            DpSize(
                (box.width * emPx).toFloat().coerceAtLeast(0f).toDp(),
                ((box.height + box.depth) * emPx).toFloat().coerceAtLeast(0f).toDp(),
            )
        }

    Canvas(modifier = modifier.size(size)) {
        drawTexBox(box = box, x = 0f, baseline = (box.height * emPx).toFloat(), emPx = emPx, color = color)
    }
}

/** Draws [box] with its origin at ([x], [baseline]). */
fun DrawScope.drawTexBox(
    box: TexBox,
    x: Float,
    baseline: Float,
    emPx: Float,
    color: Color,
) {
    drawIntoCanvas { canvas ->
        TexPainter.draw(canvas.nativeCanvas, box, x, baseline, emPx, color.toArgb())
    }
}

private object TexPainter {
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dashedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val svgPaths = WeakHashMap<TexItem.Svg, Path>()
    private val matrix = Matrix()
    private val scaledPath = Path()

    fun draw(canvas: Canvas, box: TexBox, x: Float, baseline: Float, emPx: Float, defaultColor: Int) {
        canvas.save()
        canvas.translate(x, baseline)
        box.items.forEach { item -> drawItem(canvas, item, emPx, defaultColor) }
        canvas.restore()
    }

    private fun drawItem(canvas: Canvas, item: TexItem, emPx: Float, defaultColor: Int) {
        when (item) {
            is TexItem.Glyph -> {
                glyphPaint.typeface = MathFonts.typeface(item.fontName)
                glyphPaint.textSize = (item.size * emPx).toFloat()
                glyphPaint.color = color(item.color, defaultColor)
                val gx = (item.x * emPx).toFloat()
                val gy = (item.y * emPx).toFloat()
                if (item.mirrored) {
                    canvas.save()
                    canvas.scale(-1f, 1f, gx, gy)
                    canvas.drawText(item.text, gx, gy, glyphPaint)
                    canvas.restore()
                } else {
                    canvas.drawText(item.text, gx, gy, glyphPaint)
                }
            }

            is TexItem.Rect -> {
                val left = (item.x * emPx).toFloat()
                val top = (item.y * emPx).toFloat()
                val right = ((item.x + item.width) * emPx).toFloat()
                val bottom = ((item.y + item.height) * emPx).toFloat()
                if (item.dashed) {
                    dashedPaint.color = color(item.color, defaultColor)
                    val thickness = minOf(right - left, bottom - top)
                    dashedPaint.strokeWidth = thickness
                    dashedPaint.pathEffect = DashPathEffect(floatArrayOf(thickness * 2, thickness * 2), 0f)
                    if (right - left >= bottom - top) {
                        val y = (top + bottom) / 2
                        canvas.drawLine(left, y, right, y, dashedPaint)
                    } else {
                        val lineX = (left + right) / 2
                        canvas.drawLine(lineX, top, lineX, bottom, dashedPaint)
                    }
                } else {
                    fillPaint.color = color(item.color, defaultColor)
                    canvas.drawRect(left, top, right, bottom, fillPaint)
                }
            }

            is TexItem.Line -> {
                strokePaint.color = color(item.color, defaultColor)
                strokePaint.strokeWidth = (item.strokeWidth * emPx).toFloat()
                canvas.drawLine(
                    (item.x1 * emPx).toFloat(),
                    (item.y1 * emPx).toFloat(),
                    (item.x2 * emPx).toFloat(),
                    (item.y2 * emPx).toFloat(),
                    strokePaint,
                )
            }

            is TexItem.Svg -> {
                canvas.save()
                item.clip?.let { clip ->
                    canvas.clipRect(
                        (clip.x * emPx).toFloat(),
                        (clip.y * emPx).toFloat(),
                        ((clip.x + clip.width) * emPx).toFloat(),
                        ((clip.y + clip.height) * emPx).toFloat(),
                    )
                }
                matrix.setScale(emPx, emPx)
                svgPath(item).transform(matrix, scaledPath)
                fillPaint.color = color(item.color, defaultColor)
                canvas.drawPath(scaledPath, fillPaint)
                canvas.restore()
            }
        }
    }

    /** The item's paths in ems, built once. */
    private fun svgPath(item: TexItem.Svg): Path =
        svgPaths.getOrPut(item) {
            val transform = item.transform()
            val path = Path()
            val sink =
                object : PathSink {
                    fun px(x: Double) = (transform.translateX + x * transform.scaleX).toFloat()

                    fun py(y: Double) = (transform.translateY + y * transform.scaleY).toFloat()

                    override fun moveTo(x: Double, y: Double) = path.moveTo(px(x), py(y))

                    override fun lineTo(x: Double, y: Double) = path.lineTo(px(x), py(y))

                    override fun cubicTo(x1: Double, y1: Double, x2: Double, y2: Double, x: Double, y: Double) =
                        path.cubicTo(px(x1), py(y1), px(x2), py(y2), px(x), py(y))

                    override fun quadTo(x1: Double, y1: Double, x: Double, y: Double) = path.quadTo(px(x1), py(y1), px(x), py(y))

                    override fun close() = path.close()
                }
            item.paths.forEach { SvgPath.parse(it, sink) }
            path
        }

    private fun color(css: String?, defaultColor: Int): Int = TexColor.parse(css) ?: defaultColor
}
