package com.capyreader.app.ui.articles.reader.math

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.capyreader.app.ui.articles.reader.CodeBlockElement
import com.capyreader.app.ui.articles.reader.LocalReaderStyle
import com.capyreader.app.ui.articles.reader.ReaderActions
import com.jocmp.mallet.LinearMath
import com.jocmp.mallet.LinearText
import com.jocmp.mallet.LinearTextBlockStyle

/**
 * Display math, centered. Shows the TeX source as a code block unless rendering is enabled and
 * the formula lays out.
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
    val box =
        if (readerStyle.renderMath) {
            rememberMathLayout(tex = math.tex, fontSize = readerStyle.fontSize, display = true)
        } else {
            null
        }

    if (box == null) {
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

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableWidth = maxWidth
        val scrollModifier =
            if (allowHorizontalScroll) {
                Modifier.horizontalScroll(rememberScrollState())
            } else {
                Modifier
            }

        Box(modifier = scrollModifier) {
            Box(
                modifier = Modifier.widthIn(min = availableWidth),
                contentAlignment = Alignment.Center,
            ) {
                MathFormula(
                    box = box,
                    modifier = Modifier.semantics { contentDescription = math.tex },
                )
            }
        }
    }
}
