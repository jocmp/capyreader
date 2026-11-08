package com.capyreader.app.ui.articles.reader.math

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.Density
import com.jocmp.mallet.LinearTextAnnotation
import com.jocmp.mallet.tex.TexBox
import com.jocmp.mallet.tex.TexRender

/**
 * The inline content id for a math annotation, shared by the annotated string and its content
 * map.
 */
fun inlineMathId(annotation: LinearTextAnnotation): String = "math-${annotation.start}"

/**
 * Inline content for rendered formulas at [emPx] pixels per em. Each placeholder is the formula's
 * height tall and sits on the text baseline, so the baselines line up; the depth draws below the
 * placeholder into the line spacing.
 */
fun inlineMathContent(
    renders: Map<LinearTextAnnotation, TexRender>,
    emPx: Float,
    density: Density,
    color: () -> Color,
): Map<String, InlineTextContent> =
    renders.entries.associate { (annotation, render) ->
        val box = render.box
        val placeholder =
            with(density) {
                Placeholder(
                    width = (box.width * emPx).toFloat().coerceAtLeast(0f).toSp(),
                    height = (box.height * emPx).toFloat().coerceAtLeast(1f).toSp(),
                    placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline,
                )
            }

        inlineMathId(annotation) to
            InlineTextContent(placeholder) {
                InlineFormula(box = box, emPx = emPx, color = color())
            }
    }

@Composable
private fun InlineFormula(box: TexBox, emPx: Float, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawTexBox(box = box, x = 0f, baseline = size.height, emPx = emPx, color = color)
    }
}
