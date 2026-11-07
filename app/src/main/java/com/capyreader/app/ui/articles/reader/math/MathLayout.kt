package com.capyreader.app.ui.articles.reader.math

import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import com.jocmp.mallet.LinearTextAnnotation
import com.jocmp.mallet.LinearTextAnnotationMath
import com.jocmp.mallet.math.MalletMath
import com.jocmp.mallet.math.MathBox
import com.jocmp.mallet.math.MathFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * KaTeX sets math at 1.21em of the surrounding text so its Computer Modern glyphs match the
 * x-height of typical body fonts.
 */
const val MATH_FONT_SCALE = 1.21f

/**
 * Lays out [tex], or null when it can't be rendered so callers show the source. Once the math font
 * is loaded this lays out in the same frame, so the element keeps its height; until then it lays
 * out in the background.
 */
@Composable
fun rememberMathLayout(tex: String, fontSize: TextUnit, display: Boolean): MathBox? {
    val fontSizePx = mathFontSizePx(fontSize) ?: return null
    val key = MathKey(tex = tex, fontSize = fontSizePx, display = display)
    val state = remember(key) { mutableStateOf(MathLayouts.peek(key)) }

    LaunchedEffect(key) {
        if (state.value == null) {
            state.value = MathLayouts.layout(key)
        }
    }

    return state.value?.box
}

/**
 * Inline formulas in a paragraph that rendered, keyed by their annotation.
 */
@Composable
fun rememberInlineMath(
    annotations: List<LinearTextAnnotation>,
    fontSize: TextUnit,
): Map<LinearTextAnnotation, MathBox> {
    val fontSizePx = mathFontSizePx(fontSize) ?: return emptyMap()
    val keys =
        remember(annotations, fontSizePx) {
            annotations.mapNotNull { annotation ->
                (annotation.data as? LinearTextAnnotationMath)?.let {
                    annotation to MathKey(it.tex, fontSizePx, display = false)
                }
            }
        }
    val state = remember(keys) { mutableStateOf(keys.associate { (annotation, key) -> annotation to MathLayouts.peek(key) }) }

    LaunchedEffect(keys) {
        if (state.value.values.any { it == null }) {
            state.value =
                keys.associate { (annotation, key) ->
                    annotation to (state.value[annotation] ?: MathLayouts.layout(key))
                }
        }
    }

    return remember(state.value) {
        state.value.mapNotNull { (annotation, layout) -> layout?.box?.let { annotation to it } }.toMap()
    }
}

/**
 * Loads the math font in the background so formulas lay out in the frame they first appear.
 */
suspend fun preloadMathFont() {
    withContext(Dispatchers.Default) { MathFont.default }
}

@Composable
private fun mathFontSizePx(fontSize: TextUnit): Float? {
    if (!fontSize.isSpecified) {
        return null
    }
    return with(LocalDensity.current) { fontSize.toPx() } * MATH_FONT_SCALE
}

private data class MathKey(
    val tex: String,
    val fontSize: Float,
    val display: Boolean,
)

/**
 * A finished layout. [box] is null when the TeX can't be rendered, so failures aren't retried.
 */
private class CachedLayout(val box: MathBox?)

private object MathLayouts {
    private val cache = LruCache<MathKey, CachedLayout>(256)

    /**
     * The cached layout, or a fresh one when the font is already loaded. Null means lay out in the
     * background.
     */
    fun peek(key: MathKey): CachedLayout? {
        cache.get(key)?.let { return it }
        if (!MathFont.isDefaultLoaded) {
            return null
        }
        return compute(key)
    }

    suspend fun layout(key: MathKey): CachedLayout =
        cache.get(key) ?: withContext(Dispatchers.Default) { compute(key) }

    private fun compute(key: MathKey): CachedLayout {
        val box = MalletMath.layout(key.tex, fontSize = key.fontSize, display = key.display).getOrNull()
        return CachedLayout(box).also { cache.put(key, it) }
    }
}
