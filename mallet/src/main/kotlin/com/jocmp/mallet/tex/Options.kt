package com.jocmp.mallet.tex

private val sizeStyleMap =
    listOf(
        intArrayOf(1, 1, 1),
        intArrayOf(2, 1, 1),
        intArrayOf(3, 1, 1),
        intArrayOf(4, 2, 1),
        intArrayOf(5, 2, 1),
        intArrayOf(6, 3, 1),
        intArrayOf(7, 4, 2),
        intArrayOf(8, 6, 3),
        intArrayOf(9, 7, 6),
        intArrayOf(10, 8, 7),
        intArrayOf(11, 10, 9),
    )

val sizeMultipliers = doubleArrayOf(0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.2, 1.44, 1.728, 2.074, 2.488)

private fun sizeAtStyle(size: Int, style: Style): Int = if (style.size < 2) size else sizeStyleMap[size - 1][style.size - 1]

/** The state threaded through the builder: style, size, color, and fonts. Immutable. */
class Options(
    val style: Style,
    @get:JvmName("color")
    val color: String? = null,
    size: Int? = null,
    textSize: Int? = null,
    val phantom: Boolean = false,
    val font: String = "",
    val fontFamily: String = "",
    val fontWeight: String = "",
    val fontShape: String = "",
    val maxSize: Double,
    val minRuleThickness: Double,
) {
    val size: Int = size ?: BASESIZE
    val textSize: Int = textSize ?: this.size
    val sizeMultiplier: Double = sizeMultipliers[this.size - 1]
    private val metrics by lazy { getGlobalMetrics(this.size) }

    private fun extend(
        style: Style = this.style,
        size: Int = this.size,
        textSize: Int = this.textSize,
        color: String? = this.color,
        phantom: Boolean = this.phantom,
        font: String = this.font,
        fontFamily: String = this.fontFamily,
        fontWeight: String = this.fontWeight,
        fontShape: String = this.fontShape,
    ): Options =
        Options(
            style = style,
            color = color,
            size = size,
            textSize = textSize,
            phantom = phantom,
            font = font,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontShape = fontShape,
            maxSize = maxSize,
            minRuleThickness = minRuleThickness,
        )

    fun havingStyle(style: Style): Options =
        if (this.style === style) this else extend(style = style, size = sizeAtStyle(textSize, style))

    fun havingCrampedStyle(): Options = havingStyle(style.cramp())

    fun havingSize(size: Int): Options =
        if (this.size == size && textSize == size) {
            this
        } else {
            extend(style = style.text(), size = size, textSize = size)
        }

    fun havingBaseStyle(style: Style? = null): Options {
        val target = style ?: this.style.text()
        val wantSize = sizeAtStyle(BASESIZE, target)
        return if (size == wantSize && textSize == BASESIZE && this.style === target) {
            this
        } else {
            extend(style = target, size = wantSize)
        }
    }

    fun havingBaseSizing(): Options {
        val size =
            when (style.id) {
                4, 5 -> 3
                6, 7 -> 1
                else -> 6
            }
        return extend(style = style.text(), size = size)
    }

    fun withColor(color: String?): Options = extend(color = color)

    fun withPhantom(): Options = extend(phantom = true)

    fun withFont(font: String): Options = extend(font = font)

    fun withTextFontFamily(fontFamily: String): Options = extend(fontFamily = fontFamily, font = "")

    fun withTextFontWeight(fontWeight: String): Options = extend(fontWeight = fontWeight, font = "")

    fun withTextFontShape(fontShape: String): Options = extend(fontShape = fontShape, font = "")

    fun sizingClasses(oldOptions: Options): List<String> =
        if (oldOptions.size != size) {
            listOf("katex-sizing", "reset-size${oldOptions.size}", "size$size")
        } else {
            emptyList()
        }

    fun baseSizingClasses(): List<String> =
        if (size != BASESIZE) {
            listOf("katex-sizing", "reset-size$size", "size$BASESIZE")
        } else {
            emptyList()
        }

    fun fontMetrics(): FontMetrics = metrics

    fun getColor(): String? = if (phantom) "transparent" else color

    companion object {
        const val BASESIZE = 6
    }
}
