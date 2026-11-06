package com.capyreader.app.ui.articles.reader.math

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.layout.fillMaxSize
import com.jocmp.mallet.LinearTextAnnotation
import com.jocmp.malletmath.MathBox

/**
 * The inline content id for a math annotation, shared by the annotated string and its content
 * map.
 */
fun inlineMathId(annotation: LinearTextAnnotation): String = "math-${annotation.start}"

/**
 * Inline content for rendered formulas. Each placeholder is the formula's ascent tall and sits on
 * the text baseline, so the formula's baseline lines up with the text; the descent draws below the
 * placeholder into the line spacing.
 */
fun inlineMathContent(
    boxes: Map<LinearTextAnnotation, MathBox>,
    density: Density,
    color: () -> Color,
): Map<String, InlineTextContent> =
    boxes.entries.associate { (annotation, box) ->
        val placeholder =
            with(density) {
                Placeholder(
                    width = box.width.toSp(),
                    height = box.ascent.coerceAtLeast(1f).toSp(),
                    placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline,
                )
            }

        inlineMathId(annotation) to
            InlineTextContent(placeholder) {
                InlineFormula(box = box, color = color())
            }
    }

@Composable
private fun InlineFormula(box: MathBox, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawMathBox(box = box, x = 0f, baseline = size.height, color = color)
    }
}
