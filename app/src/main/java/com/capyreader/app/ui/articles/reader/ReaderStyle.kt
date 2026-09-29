package com.capyreader.app.ui.articles.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithCurrent
import com.jocmp.capy.articles.FontOption
import com.jocmp.capy.articles.FontSize
import com.jocmp.capy.articles.TextAlignment
import org.koin.compose.koinInject

data class ReaderStyle(
    val fontFamily: FontFamily?,
    val fontSize: TextUnit,
    val titleFontFamily: FontFamily?,
    val titleFontSize: TextUnit,
    val titleAlignment: TextAlign,
    val wrapPreformattedText: Boolean,
    val showImages: Boolean,
) {
    val bodyTextStyle: TextStyle
        get() = TextStyle(
            fontFamily = fontFamily,
            fontSize = fontSize,
            lineHeight = fontSize * LINE_HEIGHT_RATIO,
        )

    val titleTextStyle: TextStyle
        get() = TextStyle(
            fontFamily = titleFontFamily,
            fontSize = titleFontSize,
            lineHeight = titleFontSize * TITLE_LINE_HEIGHT_RATIO,
            textAlign = titleAlignment,
        )

    companion object {
        private const val LINE_HEIGHT_RATIO = 1.6f
        private const val TITLE_LINE_HEIGHT_RATIO = 1.25f

        val default = ReaderStyle(
            fontFamily = null,
            fontSize = FontSize.DEFAULT.sp,
            titleFontFamily = null,
            titleFontSize = FontSize.TITLE_DEFAULT.sp,
            titleAlignment = TextAlign.Start,
            wrapPreformattedText = false,
            showImages = true,
        )
    }
}

val LocalReaderStyle = compositionLocalOf { ReaderStyle.default }

@Composable
fun paragraphSpacing(): Dp {
    val fontSize = LocalReaderStyle.current.fontSize

    return with(LocalDensity.current) { fontSize.toDp() }
}

@Composable
fun rememberReaderStyle(
    showImages: Boolean,
    appPreferences: AppPreferences = koinInject(),
): ReaderStyle {
    val options = appPreferences.readerOptions
    val fontSize by options.fontSize.collectChangesWithCurrent()
    val fontOption by options.fontFamily.collectChangesWithCurrent()
    val titleFontSize by options.titleFontSize.collectChangesWithCurrent()
    val titleAlignment by options.titleTextAlignment.collectChangesWithCurrent()
    val titleFollowsBodyFont by options.titleFollowsBodyFont.collectChangesWithCurrent()
    val horizontalPagination by options.enableHorizontaPagination.collectChangesWithCurrent()

    val bodyFont = fontOption.toFontFamily()
    val titleFont = if (titleFollowsBodyFont) {
        bodyFont
    } else {
        null
    }

    return ReaderStyle(
        fontFamily = bodyFont,
        fontSize = fontSize.sp,
        titleFontFamily = titleFont,
        titleFontSize = titleFontSize.sp,
        titleAlignment = titleAlignment.toTextAlign(),
        wrapPreformattedText = horizontalPagination,
        showImages = showImages,
    )
}

fun FontOption.toFontFamily(): FontFamily? {
    val resource = when (this) {
        FontOption.SYSTEM_DEFAULT -> return null
        FontOption.ATKINSON_HYPERLEGIBLE -> R.font.atkinson_hyperlegible
        FontOption.INTER -> R.font.inter
        FontOption.JOST -> R.font.jost
        FontOption.LITERATA -> R.font.literata
        FontOption.POPPINS -> R.font.poppins
        FontOption.VOLLKORN -> R.font.vollkorn
    }

    return FontFamily(Font(resId = resource))
}

private fun TextAlignment.toTextAlign(): TextAlign {
    return when (this) {
        TextAlignment.LEFT -> TextAlign.Start
        TextAlignment.CENTER -> TextAlign.Center
    }
}
