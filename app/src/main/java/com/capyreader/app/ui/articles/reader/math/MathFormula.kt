package com.capyreader.app.ui.articles.reader.math

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.jocmp.mallet.math.MathBox
import com.jocmp.mallet.math.MathFont
import com.jocmp.mallet.math.MathNode

/**
 * Draws a typeset formula sized to its box, with the baseline [MathBox.ascent] from the top.
 */
@Composable
fun MathFormula(
    box: MathBox,
    modifier: Modifier = Modifier,
    color: Color = LocalTextStyle.current.color.takeOrElse { LocalContentColor.current },
) {
    val size = with(LocalDensity.current) { DpSize(box.width.toDp(), box.height.toDp()) }

    Canvas(modifier = modifier.size(size)) {
        drawMathBox(box = box, x = 0f, baseline = box.ascent, color = color)
    }
}

/**
 * Draws [box] with its origin at ([x], [baseline]). Math coordinates point up; canvas
 * coordinates point down.
 */
fun DrawScope.drawMathBox(
    box: MathBox,
    x: Float,
    baseline: Float,
    color: Color,
) {
    box.children.forEach { node ->
        when (node) {
            is MathNode.Glyph -> drawGlyph(node, x = x, baseline = baseline, color = color)

            is MathNode.Rule ->
                drawRect(
                    color = color,
                    topLeft = Offset(x + node.x, baseline - node.y - node.height),
                    size = Size(node.width, node.height),
                )

            is MathNode.Child ->
                drawMathBox(
                    box = node.box,
                    x = x + node.x,
                    baseline = baseline - node.y,
                    color = node.color?.let { Color(it) } ?: color,
                )
        }
    }
}

private fun DrawScope.drawGlyph(
    node: MathNode.Glyph,
    x: Float,
    baseline: Float,
    color: Color,
) {
    val path = MathGlyphPaths.path(node.glyph)
    val scale = node.fontSize / MathFont.default.unitsPerEm

    withTransform({
        translate(left = x + node.x, top = baseline - node.y)
        scale(scaleX = scale, scaleY = -scale, pivot = Offset.Zero)
    }) {
        drawPath(path = path, color = color)
    }
}

/**
 * Glyph outlines as Compose paths in font units, built once per glyph.
 */
private object MathGlyphPaths {
    private val cache = HashMap<Int, Path>()

    fun path(glyph: Int): Path = cache.getOrPut(glyph) { build(glyph) }

    private fun build(glyph: Int): Path {
        val path = Path()

        MathFont.default.visitOutline(
            glyph,
            object : MathFont.OutlineVisitor {
                override fun moveTo(x: Float, y: Float) = path.moveTo(x, y)

                override fun lineTo(x: Float, y: Float) = path.lineTo(x, y)

                override fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) = path.quadraticTo(x1, y1, x2, y2)

                override fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
                    path.cubicTo(x1, y1, x2, y2, x3, y3)

                override fun close() = path.close()
            },
        )

        return path
    }
}
