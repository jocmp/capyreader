package com.jocmp.hyperview

/** Returns every [HtmlNode.Block.Image] in the document in document order, descending into blockquotes/lists/figures. */
fun HtmlDocument.images(): List<HtmlNode.Block.Image> {
    val out = mutableListOf<HtmlNode.Block.Image>()
    fun visit(blocks: List<HtmlNode.Block>) {
        for (block in blocks) when (block) {
            is HtmlNode.Block.Image -> out += block
            is HtmlNode.Block.Figure -> visit(block.children)
            is HtmlNode.Block.Blockquote -> visit(block.children)
            is HtmlNode.Block.UnorderedList -> block.items.forEach { visit(it.children) }
            is HtmlNode.Block.OrderedList -> block.items.forEach { visit(it.children) }
            is HtmlNode.Block.Details -> visit(block.children)
            is HtmlNode.Block.Table -> block.rows.forEach { row ->
                row.cells.forEach { cell -> visit(cell.children) }
            }
            else -> Unit
        }
    }
    visit(blocks)
    return out
}
