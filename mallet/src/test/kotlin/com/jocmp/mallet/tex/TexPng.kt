package com.jocmp.mallet.tex

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/** Renders a [TexRender] to PNG with Java2D and the app's KaTeX fonts, for eyeballing layouts. */
object TexPng {
    private val fontDir = File("../app/src/main/res/font")
    private val fonts = mutableMapOf<String, Font>()

    fun write(tex: String, file: File, displayMode: Boolean = true, pxPerEm: Double = 48.0) {
        val render = Tex.render(tex, displayMode, measurer = ::measure).getOrThrow()
        val box = render.box
        val tag = render.tag
        val pad = 0.25
        val tagGap = if (tag != null) 1.0 else 0.0
        val contentWidth = box.width + tagGap + (tag?.width ?: 0.0)
        val width = ((contentWidth + pad * 2) * pxPerEm).toInt().coerceAtLeast(1)
        val height = ((box.height + box.depth + pad * 2) * pxPerEm).toInt().coerceAtLeast(1)
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)
        g.translate(pad * pxPerEm, (pad + box.height) * pxPerEm)
        g.scale(pxPerEm, pxPerEm)
        box.items.forEach { draw(g, it) }
        if (tag != null) {
            g.translate(box.width + tagGap, 0.0)
            tag.items.forEach { draw(g, it) }
        }
        g.dispose()
        file.parentFile?.mkdirs()
        ImageIO.write(image, "png", file)
    }

    private fun measure(text: String, fontName: String): Double {
        val font = font(fontName).deriveFont(100f)
        val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
        return image.createGraphics().getFontMetrics(font).stringWidth(text) / 100.0
    }

    private fun font(name: String): Font =
        fonts.getOrPut(name) {
            val file = File(fontDir, "katex_${name.lowercase().replace('-', '_')}.ttf")
            Font.createFont(Font.TRUETYPE_FONT, file)
        }

    private fun draw(g: Graphics2D, item: TexItem) {
        when (item) {
            is TexItem.Glyph -> {
                g.color = color(item.color)
                val saved = g.transform
                g.translate(item.x, item.y)
                if (item.mirrored) {
                    g.scale(-1.0, 1.0)
                }
                g.font = font(item.fontName).deriveFont(item.size.toFloat())
                g.drawString(item.text, 0f, 0f)
                g.transform = saved
            }
            is TexItem.Rect -> {
                g.color = color(item.color)
                g.fill(Rectangle2D.Double(item.x, item.y, item.width, item.height))
            }
            is TexItem.Line -> {
                g.color = color(item.color)
                g.stroke = BasicStroke(item.strokeWidth.toFloat())
                g.draw(java.awt.geom.Line2D.Double(item.x1, item.y1, item.x2, item.y2))
            }
            is TexItem.Svg -> {
                g.color = color(item.color)
                val saved = g.clip
                item.clip?.let { g.clip(Rectangle2D.Double(it.x, it.y, it.width, it.height)) }
                val transform = item.transform()
                item.paths.forEach { data ->
                    val path = Path2D.Double()
                    SvgPath.parse(
                        data,
                        object : PathSink {
                            fun px(x: Double) = transform.translateX + x * transform.scaleX

                            fun py(y: Double) = transform.translateY + y * transform.scaleY

                            override fun moveTo(x: Double, y: Double) = path.moveTo(px(x), py(y))

                            override fun lineTo(x: Double, y: Double) = path.lineTo(px(x), py(y))

                            override fun cubicTo(x1: Double, y1: Double, x2: Double, y2: Double, x: Double, y: Double) =
                                path.curveTo(px(x1), py(y1), px(x2), py(y2), px(x), py(y))

                            override fun quadTo(x1: Double, y1: Double, x: Double, y: Double) = path.quadTo(px(x1), py(y1), px(x), py(y))

                            override fun close() = path.closePath()
                        },
                    )
                    g.fill(path)
                }
                g.clip = saved
            }
        }
    }

    private fun color(css: String?): Color = TexColor.parse(css)?.let { Color(it, true) } ?: Color.BLACK
}
