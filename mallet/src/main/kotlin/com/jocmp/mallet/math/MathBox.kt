package com.jocmp.mallet.math

/**
 * A typeset formula or part of one. Coordinates are relative to the box's baseline origin, with
 * y pointing up, in the unit of the requested font size.
 */
class MathBox(
    val width: Float,
    val ascent: Float,
    val descent: Float,
    val children: List<MathNode>,
) {
    val height: Float
        get() = ascent + descent

    internal fun shifted(down: Float): MathBox =
        MathBox(
            width = width,
            ascent = ascent - down,
            descent = descent + down,
            children = listOf(MathNode.Child(x = 0f, y = -down, box = this)),
        )

    internal fun withWidth(width: Float): MathBox = MathBox(width, ascent, descent, children)

    internal companion object {
        val EMPTY = MathBox(0f, 0f, 0f, emptyList())

        /**
         * A box sized to contain [children], like TeX's hbox.
         */
        fun containing(children: List<MathNode>): MathBox {
            var ascent = 0f
            var descent = 0f
            var width = 0f

            children.forEach { child ->
                ascent = maxOf(ascent, child.y + child.ascent)
                descent = maxOf(descent, child.descent - child.y)
                width = maxOf(width, child.x + child.width)
            }

            return MathBox(width = width, ascent = maxOf(0f, ascent), descent = maxOf(0f, descent), children = children)
        }
    }
}

sealed interface MathNode {
    val x: Float
    val y: Float

    /**
     * One glyph from [MathFont], drawn at [fontSize] with its origin at ([x], [y]).
     */
    class Glyph(
        override val x: Float,
        override val y: Float,
        val glyph: Int,
        val fontSize: Float,
        internal val width: Float,
        internal val ascent: Float,
        internal val descent: Float,
    ) : MathNode

    /**
     * A filled rectangle from ([x], [y]) to ([x] + [width], [y] + [height]).
     */
    class Rule(
        override val x: Float,
        override val y: Float,
        val width: Float,
        val height: Float,
    ) : MathNode

    /**
     * A nested box. [color] is an ARGB override from `\color` or `\textcolor`.
     */
    class Child(
        override val x: Float,
        override val y: Float,
        val box: MathBox,
        val color: Int? = null,
    ) : MathNode
}

private val MathNode.width: Float
    get() =
        when (this) {
            is MathNode.Glyph -> width
            is MathNode.Rule -> width
            is MathNode.Child -> box.width
        }

private val MathNode.ascent: Float
    get() =
        when (this) {
            is MathNode.Glyph -> ascent
            is MathNode.Rule -> height
            is MathNode.Child -> box.ascent
        }

private val MathNode.descent: Float
    get() =
        when (this) {
            is MathNode.Glyph -> descent
            is MathNode.Rule -> 0f
            is MathNode.Child -> box.descent
        }
