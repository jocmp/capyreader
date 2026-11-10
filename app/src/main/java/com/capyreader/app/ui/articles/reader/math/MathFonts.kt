package com.capyreader.app.ui.articles.reader.math

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.capyreader.app.R

/**
 * KaTeX's fonts by KaTeX font name, e.g. "Main-Regular". Glyphs a font lacks fall back to the
 * system fonts, as they do in a browser.
 */
object MathFonts {
    private val resources =
        mapOf(
            "AMS-Regular" to R.font.katex_ams_regular,
            "Caligraphic-Regular" to R.font.katex_caligraphic_regular,
            "Fraktur-Regular" to R.font.katex_fraktur_regular,
            "Main-Bold" to R.font.katex_main_bold,
            "Main-BoldItalic" to R.font.katex_main_bolditalic,
            "Main-Italic" to R.font.katex_main_italic,
            "Main-Regular" to R.font.katex_main_regular,
            "Math-BoldItalic" to R.font.katex_math_bolditalic,
            "Math-Italic" to R.font.katex_math_italic,
            "SansSerif-Bold" to R.font.katex_sansserif_bold,
            "SansSerif-Italic" to R.font.katex_sansserif_italic,
            "SansSerif-Regular" to R.font.katex_sansserif_regular,
            "Script-Regular" to R.font.katex_script_regular,
            "Size1-Regular" to R.font.katex_size1_regular,
            "Size2-Regular" to R.font.katex_size2_regular,
            "Size3-Regular" to R.font.katex_size3_regular,
            "Size4-Regular" to R.font.katex_size4_regular,
            "Typewriter-Regular" to R.font.katex_typewriter_regular,
        )

    @Volatile
    private var typefaces: Map<String, Typeface> = emptyMap()

    val isLoaded: Boolean
        get() = typefaces.isNotEmpty()

    @Synchronized
    fun load(context: Context) {
        if (isLoaded) {
            return
        }
        typefaces =
            resources.mapValues { (_, id) ->
                ResourcesCompat.getFont(context.applicationContext, id) ?: Typeface.SERIF
            }
    }

    fun typeface(fontName: String): Typeface = typefaces[fontName] ?: typefaces["Main-Regular"] ?: Typeface.SERIF

    /** Width in ems of text KaTeX has no metrics for, measured with system font fallback. */
    fun measure(text: String, fontName: String): Double {
        val paint = measurePaint.get() ?: Paint().also { measurePaint.set(it) }
        paint.typeface = typeface(fontName)
        paint.textSize = MEASURE_SIZE
        return paint.measureText(text).toDouble() / MEASURE_SIZE
    }

    private const val MEASURE_SIZE = 100f

    private val measurePaint = ThreadLocal<Paint>()
}
