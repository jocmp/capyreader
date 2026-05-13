package com.jocmp.hyperview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import androidx.compose.foundation.layout.heightIn

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: HtmlStyle = HtmlStyle.default(),
    imageLoader: ImageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
    onClick: (HtmlClick) -> Unit = {},
    onLongClick: (HtmlLongClick) -> Unit = {},
) {
    val document = remember(html) { HtmlParser.parse(html) }
    HtmlContent(
        document = document,
        modifier = modifier,
        style = style,
        imageLoader = imageLoader,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

@Composable
fun HtmlContent(
    document: HtmlDocument,
    modifier: Modifier = Modifier,
    style: HtmlStyle = HtmlStyle.default(),
    imageLoader: ImageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
    onClick: (HtmlClick) -> Unit = {},
    onLongClick: (HtmlLongClick) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(style.blockSpacing),
    ) {
        for (block in document.blocks) {
            RenderBlock(
                block = block,
                style = style,
                imageLoader = imageLoader,
                onClick = onClick,
                onLongClick = onLongClick,
            )
        }
    }
}

@Composable
private fun RenderBlock(
    block: HtmlNode.Block,
    style: HtmlStyle,
    imageLoader: ImageLoader,
    onClick: (HtmlClick) -> Unit,
    onLongClick: (HtmlLongClick) -> Unit,
) {
    when (block) {
        is HtmlNode.Block.Paragraph -> RenderInlines(
            inlines = block.inlines,
            textStyle = style.body,
            style = style,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        is HtmlNode.Block.Heading -> RenderInlines(
            inlines = block.inlines,
            textStyle = style.heading(block.level).withFontWeight(FontWeight.Bold),
            style = style,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        is HtmlNode.Block.Blockquote -> Blockquote(block, style, imageLoader, onClick, onLongClick)
        is HtmlNode.Block.UnorderedList -> ListBlock(
            items = block.items,
            marker = { "•" },
            style = style,
            imageLoader = imageLoader,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        is HtmlNode.Block.OrderedList -> ListBlock(
            items = block.items,
            marker = { index -> "${block.start + index}." },
            style = style,
            imageLoader = imageLoader,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        is HtmlNode.Block.CodeBlock -> CodeBlockView(block, style)
        HtmlNode.Block.HorizontalRule -> HorizontalDivider(color = style.horizontalRuleColor)
        is HtmlNode.Block.Image -> ImageBlock(block, imageLoader, onClick, onLongClick)
        is HtmlNode.Block.Figure -> FigureBlock(block, style, imageLoader, onClick, onLongClick)
        is HtmlNode.Block.Table -> TableBlock(block, style, imageLoader, onClick, onLongClick)
        is HtmlNode.Block.Video,
        is HtmlNode.Block.Audio,
        is HtmlNode.Block.Iframe,
        is HtmlNode.Block.Details -> Unit // wired up in later milestones
    }
}

@Composable
private fun TableBlock(
    table: HtmlNode.Block.Table,
    style: HtmlStyle,
    imageLoader: ImageLoader,
    onClick: (HtmlClick) -> Unit,
    onLongClick: (HtmlLongClick) -> Unit,
) {
    val borderColor = style.horizontalRuleColor
    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in table.rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (cell in row.cells) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(width = 1.dp, color = borderColor)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(style.blockSpacing / 2),
                    ) {
                        for (child in cell.children) {
                            if (cell.header || row.header) {
                                BoldifiedBlock(child, style, imageLoader, onClick, onLongClick)
                            } else {
                                RenderBlock(child, style, imageLoader, onClick, onLongClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoldifiedBlock(
    block: HtmlNode.Block,
    style: HtmlStyle,
    imageLoader: ImageLoader,
    onClick: (HtmlClick) -> Unit,
    onLongClick: (HtmlLongClick) -> Unit,
) {
    val boldened = style.copy(body = style.body.withFontWeight(FontWeight.SemiBold))
    RenderBlock(block, boldened, imageLoader, onClick, onLongClick)
}

@Composable
private fun RenderInlines(
    inlines: List<HtmlNode.Inline>,
    textStyle: androidx.compose.ui.text.TextStyle,
    style: HtmlStyle,
    onClick: (HtmlClick) -> Unit,
    onLongClick: (HtmlLongClick) -> Unit = {},
) {
    if (inlines.isEmpty()) return
    val annotated = remember(inlines, style) { inlines.toAnnotatedString(style) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    fun linkAt(position: androidx.compose.ui.geometry.Offset): Pair<String, String>? {
        val result = layout ?: return null
        val offset = result.getOffsetForPosition(position)
        val href = annotated.getStringAnnotations(URL_ANNOTATION_TAG, offset, offset)
            .firstOrNull()?.item ?: return null
        val text = annotated.getStringAnnotations(LINK_TEXT_ANNOTATION_TAG, offset, offset)
            .firstOrNull()?.item.orEmpty()
        return href to text
    }

    Text(
        text = annotated,
        style = textStyle,
        onTextLayout = { layout = it },
        modifier = Modifier.pointerInput(annotated) {
            detectTapGestures(
                onTap = { position ->
                    linkAt(position)?.let { (href, text) -> onClick(HtmlClick.Link(href, text)) }
                },
                onLongPress = { position ->
                    linkAt(position)?.let { (href, text) ->
                        onLongClick(HtmlLongClick.Link(href, text))
                    }
                },
            )
        },
    )
}

@Composable
private fun Blockquote(
    block: HtmlNode.Block.Blockquote,
    style: HtmlStyle,
    imageLoader: ImageLoader,
    onClick: (HtmlClick) -> Unit,
    onLongClick: (HtmlLongClick) -> Unit,
) {
    Row {
        Box(
            modifier = Modifier
                .width(style.blockquoteBarWidth)
                .background(style.blockquoteBar)
                .padding(end = 12.dp)
        )
        Column(
            modifier = Modifier.padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(style.blockSpacing),
        ) {
            for (child in block.children) {
                RenderBlock(child, style, imageLoader, onClick, onLongClick)
            }
        }
    }
}

@Composable
private fun ListBlock(
    items: List<HtmlNode.Block.ListItem>,
    marker: (index: Int) -> String,
    style: HtmlStyle,
    imageLoader: ImageLoader,
    onClick: (HtmlClick) -> Unit,
    onLongClick: (HtmlLongClick) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(style.blockSpacing / 2)) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.Top) {
                Text(
                    text = marker(index),
                    style = style.body,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(style.blockSpacing / 2),
                    modifier = Modifier.weight(1f),
                ) {
                    for (child in item.children) {
                        RenderBlock(child, style, imageLoader, onClick, onLongClick)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageBlock(
    image: HtmlNode.Block.Image,
    imageLoader: ImageLoader,
    onClick: (HtmlClick) -> Unit,
    onLongClick: (HtmlLongClick) -> Unit,
) {
    val src = image.bestSrc()
    if (src.isBlank()) return

    val targetWidthPx = rememberTargetWidthPx()
    val density = LocalDensity.current.density
    val resolved = remember(image, targetWidthPx, density) {
        image.bestUrl(targetWidthPx = targetWidthPx, pixelDensity = density)
    }

    val aspect = image.intrinsicAspect()
    val placeholderBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val clickModifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = { onClick(HtmlClick.Image(resolved, image.alt)) },
            onLongClick = { onLongClick(HtmlLongClick.Image(resolved, image.alt)) },
        )

    // Reserve a stable height: explicit aspect when intrinsic dims are known,
    // otherwise a minimum so loading/failed images are visible instead of 0-tall.
    val sizedModifier = when {
        aspect != null -> clickModifier.aspectRatio(aspect)
        else -> clickModifier.heightIn(min = 120.dp)
    }

    SubcomposeAsyncImage(
        model = resolved,
        contentDescription = image.alt,
        imageLoader = imageLoader,
        contentScale = ContentScale.FillWidth,
        modifier = sizedModifier.background(placeholderBg),
    ) {
        when (painter.state.collectAsState().value) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            else -> Unit // placeholder background shows through
        }
    }
}

@Composable
private fun FigureBlock(
    figure: HtmlNode.Block.Figure,
    style: HtmlStyle,
    imageLoader: ImageLoader,
    onClick: (HtmlClick) -> Unit,
    onLongClick: (HtmlLongClick) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(style.blockSpacing / 2)) {
        for (child in figure.children) {
            RenderBlock(child, style, imageLoader, onClick, onLongClick)
        }
        if (!figure.caption.isNullOrEmpty()) {
            val captionStyle = style.body.copy(
                fontSize = style.body.fontSize * 0.85f,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RenderInlines(figure.caption, captionStyle, style, onClick)
        }
    }
}

@Composable
private fun rememberTargetWidthPx(): Int {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    return remember(configuration.screenWidthDp, density.density) {
        (configuration.screenWidthDp * density.density).toInt()
    }
}

private fun HtmlNode.Block.Image.bestSrc(): String =
    src.ifBlank { sources.firstOrNull()?.url.orEmpty() }

private fun HtmlNode.Block.Image.intrinsicAspect(): Float? {
    val w = width ?: return null
    val h = height ?: return null
    if (w <= 0 || h <= 0) return null
    return w.toFloat() / h.toFloat()
}

@Composable
private fun CodeBlockView(block: HtmlNode.Block.CodeBlock, style: HtmlStyle) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(style.codeBlockBackground)
    ) {
        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Text(
                text = block.text,
                style = style.code,
                modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
            )
        }
    }
}
