package com.capyreader.app.ui.articles.reader.math

import android.content.Context
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import com.jocmp.mallet.LinearTextAnnotation
import com.jocmp.mallet.LinearTextAnnotationMath
import com.jocmp.mallet.tex.Tex
import com.jocmp.mallet.tex.TexRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lays out [tex], or null when KaTeX rejects it so callers show the source. Once the math fonts
 * are loaded this lays out in the same frame, so the element keeps its height; until then it lays
 * out in the background.
 */
@Composable
fun rememberMathLayout(tex: String, display: Boolean): TexRender? {
    val context = LocalContext.current
    val key = MathKey(tex = tex, display = display)
    val state = remember(key) { mutableStateOf(MathLayouts.peek(key)) }

    LaunchedEffect(key) {
        if (state.value == null) {
            state.value = MathLayouts.layout(context, key)
        }
    }

    return state.value?.render
}

/**
 * Inline formulas in a paragraph that rendered, keyed by their annotation.
 */
@Composable
fun rememberInlineMath(annotations: List<LinearTextAnnotation>): Map<LinearTextAnnotation, TexRender> {
    val context = LocalContext.current
    val keys =
        remember(annotations) {
            annotations.mapNotNull { annotation ->
                (annotation.data as? LinearTextAnnotationMath)?.let {
                    annotation to MathKey(it.tex, display = false)
                }
            }
        }
    val state = remember(keys) { mutableStateOf(keys.associate { (annotation, key) -> annotation to MathLayouts.peek(key) }) }

    LaunchedEffect(keys) {
        if (state.value.values.any { it == null }) {
            state.value =
                keys.associate { (annotation, key) ->
                    annotation to (state.value[annotation] ?: MathLayouts.layout(context, key))
                }
        }
    }

    return remember(state.value) {
        state.value.mapNotNull { (annotation, layout) -> layout?.render?.let { annotation to it } }.toMap()
    }
}

/**
 * Pixels per em for math set beside text of [fontSize], scaled up as KaTeX does.
 */
@Composable
fun mathEmPx(fontSize: TextUnit): Float? {
    if (!fontSize.isSpecified) {
        return null
    }
    return with(LocalDensity.current) { fontSize.toPx() } * Tex.FONT_SCALE.toFloat()
}

/**
 * Loads the math fonts, metrics, and KaTeX's function and macro tables in the background, so
 * formulas lay out in the frame they first appear without that one-time cost.
 */
suspend fun preloadMathFonts(context: Context) {
    withContext(Dispatchers.IO) {
        Tex.render("x", displayMode = false)
        MathFonts.load(context)
    }
}

private data class MathKey(
    val tex: String,
    val display: Boolean,
)

/**
 * A finished layout. [render] is null when the TeX can't be rendered, so failures aren't retried.
 */
private class CachedLayout(val render: TexRender?)

private object MathLayouts {
    private val cache = LruCache<MathKey, CachedLayout>(256)

    /**
     * The cached layout, or a fresh one when the fonts are already loaded. Null means lay out in
     * the background.
     */
    fun peek(key: MathKey): CachedLayout? {
        cache.get(key)?.let { return it }
        if (!MathFonts.isLoaded) {
            return null
        }
        return compute(key)
    }

    suspend fun layout(context: Context, key: MathKey): CachedLayout =
        cache.get(key) ?: withContext(Dispatchers.Default) {
            MathFonts.load(context)
            compute(key)
        }

    private fun compute(key: MathKey): CachedLayout {
        val render = Tex.render(key.tex, displayMode = key.display, measurer = MathFonts::measure).getOrNull()
        return CachedLayout(render).also { cache.put(key, it) }
    }
}
