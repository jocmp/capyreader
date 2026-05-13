package com.jocmp.hyperview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class HtmlStyle(
    val body: TextStyle,
    val heading: (level: Int) -> TextStyle,
    val link: TextStyle,
    val code: TextStyle,
    val codeBlockBackground: Color,
    val blockquoteBar: Color,
    val blockquoteBarWidth: Dp = 4.dp,
    val blockSpacing: Dp = 12.dp,
    val listIndent: Dp = 24.dp,
    val codeBlockFontFamily: FontFamily = FontFamily.Monospace,
    val horizontalRuleColor: Color,
) {
    companion object {
        @Composable
        @ReadOnlyComposable
        fun default(): HtmlStyle {
            val typography = MaterialTheme.typography
            val colorScheme = MaterialTheme.colorScheme
            return HtmlStyle(
                body = typography.bodyLarge,
                heading = { level ->
                    when (level) {
                        1 -> typography.headlineLarge
                        2 -> typography.headlineMedium
                        3 -> typography.headlineSmall
                        4 -> typography.titleLarge
                        5 -> typography.titleMedium
                        else -> typography.titleSmall
                    }
                },
                link = typography.bodyLarge.copy(color = colorScheme.primary),
                code = typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                ),
                codeBlockBackground = colorScheme.surfaceVariant,
                blockquoteBar = colorScheme.outlineVariant,
                horizontalRuleColor = colorScheme.outlineVariant,
            )
        }
    }
}

internal fun TextStyle.withFontWeight(weight: FontWeight): TextStyle =
    if (fontWeight == weight) this else copy(fontWeight = weight)
