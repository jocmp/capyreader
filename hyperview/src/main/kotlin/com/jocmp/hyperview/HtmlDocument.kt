package com.jocmp.hyperview

data class HtmlDocument(val blocks: List<HtmlNode.Block>)

sealed interface HtmlNode {

    sealed interface Block : HtmlNode {

        data class Paragraph(val inlines: List<Inline>) : Block

        data class Heading(val level: Int, val inlines: List<Inline>) : Block

        data class Blockquote(val children: List<Block>) : Block

        data class UnorderedList(val items: List<ListItem>) : Block

        data class OrderedList(val items: List<ListItem>, val start: Int = 1) : Block

        data class ListItem(val children: List<Block>)

        data class CodeBlock(val text: String, val language: String? = null) : Block

        data class Figure(val children: List<Block>, val caption: List<Inline>? = null) : Block

        object HorizontalRule : Block {
            override fun toString() = "HorizontalRule"
        }

        data class Image(
            val src: String,
            val alt: String? = null,
            val title: String? = null,
            val width: Int? = null,
            val height: Int? = null,
            val sources: List<ImageSource> = emptyList(),
            val sizes: String? = null,
        ) : Block

        data class ImageSource(
            val url: String,
            val width: Int? = null,
            val density: Float? = null,
            val mediaType: String? = null,
            val media: String? = null,
        )

        data class Video(
            val src: String?,
            val poster: String? = null,
            val sources: List<Source> = emptyList(),
        ) : Block {
            data class Source(val src: String, val type: String?)
        }

        data class Audio(
            val src: String?,
            val sources: List<Video.Source> = emptyList(),
        ) : Block

        data class Iframe(val src: String, val title: String? = null) : Block

        data class Table(val rows: List<Row>) : Block {
            data class Row(val cells: List<Cell>, val header: Boolean = false)
            data class Cell(val children: List<Block>, val header: Boolean = false)
        }

        data class Details(val summary: List<Inline>, val children: List<Block>) : Block
    }

    sealed interface Inline : HtmlNode {

        data class Text(val text: String, val style: InlineStyle = InlineStyle.NONE) : Inline

        data class Link(
            val href: String,
            val children: List<Inline>,
            val title: String? = null,
        ) : Inline

        data class Code(val text: String) : Inline

        object LineBreak : Inline {
            override fun toString() = "LineBreak"
        }
    }
}

@JvmInline
value class InlineStyle(val mask: Int) {
    fun has(flag: InlineStyle): Boolean = (mask and flag.mask) == flag.mask
    operator fun plus(other: InlineStyle): InlineStyle = InlineStyle(mask or other.mask)

    companion object {
        val NONE = InlineStyle(0)
        val BOLD = InlineStyle(1 shl 0)
        val ITALIC = InlineStyle(1 shl 1)
        val UNDERLINE = InlineStyle(1 shl 2)
        val STRIKETHROUGH = InlineStyle(1 shl 3)
        val SUBSCRIPT = InlineStyle(1 shl 4)
        val SUPERSCRIPT = InlineStyle(1 shl 5)
        val MARK = InlineStyle(1 shl 6)
        val SMALL = InlineStyle(1 shl 7)
    }
}
