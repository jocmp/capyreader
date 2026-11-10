package com.capyreader.app.ui.articles.reader.math

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.articles.reader.CodeBlockElement
import com.capyreader.app.ui.articles.reader.LocalReaderStyle
import com.capyreader.app.ui.articles.reader.ReaderActions
import com.jocmp.mallet.LinearMath
import com.jocmp.mallet.LinearText
import com.jocmp.mallet.LinearTextBlockStyle
import com.jocmp.mallet.tex.TexRender

/**
 * Display math, centered with any `\tag` at the end. Shows the TeX source as a code block unless
 * rendering is enabled and the formula lays out.
 */
@Composable
fun MathElement(
    math: LinearMath,
    idToIndex: Map<String, Int>,
    actions: ReaderActions,
    allowHorizontalScroll: Boolean,
    modifier: Modifier = Modifier,
) {
    val readerStyle = LocalReaderStyle.current
    val emPx = mathEmPx(fontSize = readerStyle.fontSize)
    val render =
        if (readerStyle.renderMath && emPx != null) {
            rememberMathLayout(tex = math.tex, display = true)
        } else {
            null
        }

    if (render == null || emPx == null) {
        CodeBlockElement(
            linearText =
                LinearText(
                    ids = math.ids,
                    text = math.tex,
                    annotations = emptyList(),
                    blockStyle = LinearTextBlockStyle.CODE_BLOCK,
                ),
            idToIndex = idToIndex,
            actions = actions,
            allowHorizontalScroll = allowHorizontalScroll,
            modifier = modifier,
        )
        return
    }

    val color = LocalTextStyle.current.color.takeOrElse { LocalContentColor.current }
    val scrollable = allowHorizontalScroll && !readerStyle.wrapPreformattedText
    // Table cells disallow scrolling and are measured at the table's width, so size to content there.
    val fillWidth = allowHorizontalScroll
    val tagGap = 16.dp

    BoxWithConstraints(modifier = if (fillWidth) modifier.fillMaxWidth() else modifier) {
        val availableWidth = maxWidth
        val density = LocalDensity.current
        val tagGapPx = if (render.tag != null) with(density) { tagGap.toPx() } else 0f
        val formulaEmPx =
            if (scrollable) {
                emPx
            } else {
                fitEmPx(render = render, emPx = emPx, maxWidthPx = constraints.maxWidth.toFloat(), tagGapPx = tagGapPx)
            }
        val contentWidth = with(density) { ((render.width * formulaEmPx).toFloat() + tagGapPx).toDp() }
        val scrollModifier =
            if (scrollable) {
                Modifier.horizontalScroll(rememberScrollState())
            } else {
                Modifier
            }

        Box(modifier = scrollModifier) {
            Box(modifier = Modifier.widthIn(min = if (fillWidth) availableWidth else contentWidth)) {
                MathFormula(
                    box = render.box,
                    emPx = formulaEmPx,
                    color = color,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .semantics { contentDescription = math.tex },
                )
                render.tag?.let { tag ->
                    MathFormula(
                        box = tag,
                        emPx = formulaEmPx,
                        color = color,
                        modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .padding(start = tagGap),
                    )
                }
            }
        }
    }
}

/** Width in ems of the formula plus its tag. */
private val TexRender.width: Double
    get() = box.width + (tag?.width ?: 0.0)

/** [emPx] shrunk so the formula and tag fit in [maxWidthPx] when they can't scroll. */
private fun fitEmPx(render: TexRender, emPx: Float, maxWidthPx: Float, tagGapPx: Float): Float {
    val width = (render.width * emPx).toFloat()
    val available = maxWidthPx - tagGapPx
    if (width <= available || width <= 0f || available <= 0f) {
        return emPx
    }
    return emPx * available / width
}
