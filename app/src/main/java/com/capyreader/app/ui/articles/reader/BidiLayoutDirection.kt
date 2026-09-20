package com.capyreader.app.ui.articles.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.text.Bidi

@Composable
fun BidiLayoutDirection(
    paragraph: String,
    content: @Composable () -> Unit,
) {
    val direction = remember(paragraph) {
        val bidi = Bidi(paragraph, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)

        if (bidi.baseIsLeftToRight()) {
            LayoutDirection.Ltr
        } else {
            LayoutDirection.Rtl
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        content()
    }
}
