package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Size
import com.capyreader.app.R
import com.capyreader.app.ui.components.ShareLink
import com.jocmp.mallet.LinearAudio
import com.jocmp.mallet.LinearBlockQuote
import com.jocmp.mallet.LinearElement
import com.jocmp.mallet.LinearImage
import com.jocmp.mallet.LinearListItem
import com.jocmp.mallet.LinearTable
import com.jocmp.mallet.LinearTableCellItemType
import com.jocmp.mallet.LinearText
import com.jocmp.mallet.LinearTextAnnotation
import com.jocmp.mallet.LinearTextAnnotationLink
import com.jocmp.mallet.LinearTextBlockStyle
import com.jocmp.mallet.LinearVideo
import com.jocmp.mallet.toTableData

private const val MAX_IMAGE_WIDTH_PX = 2000
private const val PLACEHOLDER_ASPECT_RATIO = 3f / 2f
private val DEFAULT_VIDEO_ASPECT_RATIO = 16f / 9f
private val CORNER_SHAPE = RoundedCornerShape(3.dp)

@Composable
fun ArticleElement(
    element: LinearElement,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    allowHorizontalScroll: Boolean,
    modifier: Modifier = Modifier,
) {
    when (element) {
        is LinearText -> {
            if (element.blockStyle == LinearTextBlockStyle.TEXT) {
                ParagraphElement(
                    linearText = element,
                    idToIndex = idToIndex,
                    actions = actions,
                    modifier = modifier,
                )
            } else {
                CodeBlockElement(
                    linearText = element,
                    idToIndex = idToIndex,
                    actions = actions,
                    allowHorizontalScroll = allowHorizontalScroll,
                    modifier = modifier,
                )
            }
        }

        is LinearImage -> DisableSelection {
            ImageElement(
                image = element,
                idToIndex = idToIndex,
                actions = actions,
                modifier = modifier,
            )
        }

        is LinearBlockQuote -> BlockQuoteElement(
            blockQuote = element,
            idToIndex = idToIndex,
            actions = actions,
            allowHorizontalScroll = allowHorizontalScroll,
            modifier = modifier,
        )

        is LinearListItem -> ListItemElement(
            listItem = element,
            idToIndex = idToIndex,
            actions = actions,
            allowHorizontalScroll = allowHorizontalScroll,
            modifier = modifier,
        )

        is LinearTable -> TableElement(
            table = element,
            idToIndex = idToIndex,
            actions = actions,
            allowHorizontalScroll = allowHorizontalScroll,
            modifier = modifier,
        )

        is LinearVideo -> DisableSelection {
            VideoElement(
                video = element,
                actions = actions,
                modifier = modifier,
            )
        }

        is LinearAudio -> DisableSelection {
            AudioElement(
                audio = element,
                actions = actions,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun ParagraphElement(
    linearText: LinearText,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    modifier: Modifier = Modifier,
) {
    val headingScale = linearText.headingScale

    if (headingScale == null) {
        TextElement(
            linearText = linearText,
            idToIndex = idToIndex,
            actions = actions,
            modifier = modifier,
        )
        return
    }

    val base = LocalTextStyle.current
    val headingStyle = if (base.fontSize.isSpecified) {
        base.copy(
            fontSize = base.fontSize * headingScale,
            lineHeight = base.fontSize * headingScale * 1.3f,
            fontWeight = FontWeight.Bold,
        )
    } else {
        base.copy(fontWeight = FontWeight.Bold)
    }

    ProvideTextStyle(headingStyle) {
        TextElement(
            linearText = linearText,
            idToIndex = idToIndex,
            actions = actions,
            modifier = modifier,
        )
    }
}

@Composable
fun TextElement(
    linearText: LinearText,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    modifier: Modifier = Modifier,
    softWrap: Boolean = true,
) {
    val annotated = linearText.toAnnotatedString(
        idToIndex = idToIndex,
        onLinkClick = actions.onLinkClick,
    )
    val links = remember(linearText) { linearText.links }
    val haptics = LocalHapticFeedback.current
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    BidiLayoutDirection(paragraph = linearText.text) {
        Text(
            text = annotated,
            softWrap = softWrap,
            onTextLayout = { layout = it },
            modifier = modifier.longPressLink(
                enabled = links.isNotEmpty(),
                linkAt = { position -> layout.linkAt(position, links) },
                onLongPress = { link ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                    actions.onLinkLongPress(
                        ShareLink(
                            text = linearText.text.substring(link.start, link.endExclusive),
                            url = (link.data as LinearTextAnnotationLink).href,
                        )
                    )
                },
            ),
        )
    }
}

fun resolveAnchorIndex(href: String, idToIndex: Map<String, Int>): Int? {
    val fragment = href.substringAfter('#', missingDelimiterValue = "")

    if (fragment.isEmpty()) {
        return null
    }

    return idToIndex[fragment]
}

@Composable
fun CodeBlockElement(
    linearText: LinearText,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    allowHorizontalScroll: Boolean,
    modifier: Modifier = Modifier,
) {
    val wrap = LocalReaderStyle.current.wrapPreformattedText
    val scrollModifier = if (allowHorizontalScroll && !wrap) {
        Modifier.horizontalScroll(rememberScrollState())
    } else {
        Modifier
    }
    val base = LocalTextStyle.current
    val codeStyle = if (base.fontSize.isSpecified) {
        base.copy(fontFamily = FontFamily.Monospace, fontSize = base.fontSize * 0.9f)
    } else {
        base.copy(fontFamily = FontFamily.Monospace)
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = CORNER_SHAPE,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = scrollModifier.padding(16.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            ProvideTextStyle(codeStyle) {
                TextElement(
                    linearText = linearText,
                    idToIndex = idToIndex,
                    actions = actions,
                    softWrap = wrap,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageElement(
    image: LinearImage,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    modifier: Modifier = Modifier,
) {
    val readerStyle = LocalReaderStyle.current

    if (!readerStyle.showImages || image.sources.isEmpty()) {
        return
    }

    val density = LocalDensity.current
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxWidthPx = with(density) { maxWidth.roundToPx() }.coerceIn(1, MAX_IMAGE_WIDTH_PX)
        val source = remember(image, maxWidthPx) {
            image.bestSource(pixelDensity = density.density, maxWidthPx = maxWidthPx)
        }

        if (source == null) {
            return@BoxWithConstraints
        }

        val requestWidth = source.requestWidth(maxWidthPx)
        val requestHeight = source.heightPx ?: requestWidth
        var failed by remember(source.imgUri) { mutableStateOf(false) }

        val aspectRatio = source.aspectRatio
            ?: ImageAspectRatios[source.imgUri]
            ?: PLACEHOLDER_ASPECT_RATIO

        val sizeModifier = if (failed) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(source.imgUri)
                    .size(Size(requestWidth, requestHeight))
                    .precision(Precision.INEXACT)
                    .build(),
                contentDescription = image.caption?.text,
                contentScale = RestrainedFillWidthScaling(density.density),
                onSuccess = { state ->
                    failed = false

                    ImageAspectRatios.put(
                        url = source.imgUri,
                        width = state.result.image.width,
                        height = state.result.image.height,
                    )
                },
                onError = { failed = true },
                modifier = sizeModifier
                    .combinedClickable(
                        onClick = { actions.onImageClick(image) },
                        onLongClick = { actions.onImageLongPress(source.imgUri) },
                    ),
            )

            image.caption?.let { caption ->
                CaptionText(
                    linearText = caption,
                    idToIndex = idToIndex,
                    actions = actions,
                )
            }
        }
    }
}

@Composable
private fun CaptionText(
    linearText: LinearText,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
) {
    val base = LocalTextStyle.current
    val captionStyle = if (base.fontSize.isSpecified) {
        base.copy(
            fontSize = base.fontSize * 0.75f,
            lineHeight = base.fontSize * 0.75f * 1.2f,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        base.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    ProvideTextStyle(captionStyle) {
        TextElement(
            linearText = linearText,
            idToIndex = idToIndex,
            actions = actions,
        )
    }
}

@Composable
fun BlockQuoteElement(
    blockQuote: LinearBlockQuote,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    allowHorizontalScroll: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(paragraphSpacing()),
            modifier = Modifier
                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                .fillMaxWidth(),
        ) {
            blockQuote.content.forEach { element ->
                ArticleElement(
                    element = element,
                    idToIndex = idToIndex,
                    actions = actions,
                    allowHorizontalScroll = allowHorizontalScroll,
                )
            }
        }
    }
}

@Composable
fun ListItemElement(
    listItem: LinearListItem,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    allowHorizontalScroll: Boolean,
    modifier: Modifier = Modifier,
) {
    val marker = if (listItem.orderedIndex != null) {
        "${listItem.orderedIndex}."
    } else {
        "•"
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(start = 8.dp),
    ) {
        Text(text = marker)
        Column(
            verticalArrangement = Arrangement.spacedBy(paragraphSpacing()),
            modifier = Modifier.fillMaxWidth(),
        ) {
            listItem.content.forEach { element ->
                ArticleElement(
                    element = element,
                    idToIndex = idToIndex,
                    actions = actions,
                    allowHorizontalScroll = allowHorizontalScroll,
                )
            }
        }
    }
}

@Composable
fun TableElement(
    table: LinearTable,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    allowHorizontalScroll: Boolean,
    modifier: Modifier = Modifier,
) {
    val tableData = remember(table) { table.toTableData() }
    val wrap = LocalReaderStyle.current.wrapPreformattedText
    val borderColor = MaterialTheme.colorScheme.outline

    TableLayout(
        tableData = tableData,
        allowHorizontalScroll = allowHorizontalScroll && !wrap,
        modifier = modifier,
    ) { row, column ->
        val cell = table.cellAt(row = row, col = column)

        if (cell != null) {
            val isHeader = cell.type == LinearTableCellItemType.HEADER
            val cellStyle = if (isHeader) {
                LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
            } else {
                LocalTextStyle.current
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .border(width = 1.dp, color = borderColor)
                    .padding(4.dp),
            ) {
                ProvideTextStyle(cellStyle) {
                    cell.content.forEach { element ->
                        ArticleElement(
                            element = element,
                            idToIndex = idToIndex,
                            actions = actions,
                            allowHorizontalScroll = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoElement(
    video: LinearVideo,
    actions: ReaderActions,
    modifier: Modifier = Modifier,
) {
    val source = video.firstSource
    val showImages = LocalReaderStyle.current.showImages
    val thumbnail = video.imageThumbnail
    val label = stringResource(R.string.reader_play_video)
    val videoID = source.youtubeVideoID
    var isPlaying by rememberSaveable(source.uri) { mutableStateOf(false) }

    val aspectRatio = if (videoID == null) {
        source.aspectRatio ?: DEFAULT_VIDEO_ASPECT_RATIO
    } else {
        DEFAULT_VIDEO_ASPECT_RATIO
    }

    val frame = modifier
        .fillMaxWidth()
        .aspectRatio(aspectRatio)
        .clip(CORNER_SHAPE)
        .background(Color.Black)

    if (isPlaying && videoID != null) {
        YoutubePlayer(
            videoID = videoID,
            modifier = frame,
        )
        return
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = frame.clickable {
            if (videoID == null) {
                actions.onLinkClick(source.link, null)
            } else {
                isPlaying = true
            }
        },
    ) {
        if (thumbnail != null && showImages) {
            AsyncImage(
                model = thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
fun AudioElement(
    audio: LinearAudio,
    actions: ReaderActions,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable { actions.onAudioClick(audio.firstSource.uri) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Audiotrack,
                contentDescription = null,
                tint = LocalContentColor.current,
            )
            Text(text = stringResource(R.string.reader_play_audio))
        }
    }
}

private val com.jocmp.mallet.LinearVideoSource.aspectRatio: Float?
    get() {
        val width = widthPx ?: return null
        val height = heightPx ?: return null

        return width.toFloat() / height.toFloat()
    }
