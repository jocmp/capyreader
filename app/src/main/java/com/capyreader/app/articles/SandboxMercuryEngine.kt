package com.capyreader.app.articles

import android.content.Context
import androidx.concurrent.futures.await
import androidx.javascriptengine.IsolateStartupParameters
import androidx.javascriptengine.IsolateTerminatedException
import androidx.javascriptengine.JavaScriptIsolate
import androidx.javascriptengine.JavaScriptSandbox
import androidx.javascriptengine.SandboxDeadException
import com.jocmp.capy.articles.KotlinMercuryEngine
import com.jocmp.capy.articles.MercuryEngine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

/**
 * Runs the JS Mercury Parser in the WebView's out-of-process V8 sandbox.
 * Uses [fallback] when the sandbox is unavailable or the isolate dies,
 * e.g. when a page exceeds the heap limit.
 */
class SandboxMercuryEngine(
    private val context: Context,
    private val fallback: MercuryEngine = KotlinMercuryEngine,
) : MercuryEngine {
    private val mutex = Mutex()
    private var sandbox: JavaScriptSandbox? = null
    private var isolate: JavaScriptIsolate? = null
    private var isUnsupported = false

    override suspend fun parse(url: String, html: String): MercuryEngine.Result {
        val isolate = mutex.withLock { connect() } ?: return fallback.parse(url, html)

        val script = "capyMercury.parse(${JSONObject.quote(url)}, ${JSONObject.quote(html)})"

        val json = try {
            isolate.evaluateJavaScriptAsync(script).await()
        } catch (e: IsolateTerminatedException) {
            reset()
            return fallback.parse(url, html)
        } catch (e: SandboxDeadException) {
            reset()
            return fallback.parse(url, html)
        }

        return decode(json)
    }

    /** Connects to the sandbox and loads the parser. Returns false when unsupported. */
    suspend fun prepare(): Boolean {
        return mutex.withLock { connect() } != null
    }

    suspend fun close() {
        reset()
    }

    private suspend fun connect(): JavaScriptIsolate? {
        isolate?.let { return it }

        if (isUnsupported || !JavaScriptSandbox.isSupported()) {
            isUnsupported = true
            return null
        }

        val sandbox = JavaScriptSandbox.createConnectedInstanceAsync(context.applicationContext).await()

        if (REQUIRED_FEATURES.any { !sandbox.isFeatureSupported(it) }) {
            sandbox.close()
            isUnsupported = true
            return null
        }

        val parameters = IsolateStartupParameters().apply {
            if (sandbox.isFeatureSupported(JavaScriptSandbox.JS_FEATURE_ISOLATE_MAX_HEAP_SIZE)) {
                maxHeapSizeBytes = MAX_HEAP_SIZE_BYTES
            }
        }

        val isolate = sandbox.createIsolate(parameters)
        isolate.evaluateJavaScriptAsync(loadBundle()).await()

        this.sandbox = sandbox
        this.isolate = isolate

        return isolate
    }

    private fun loadBundle(): String {
        return context.assets.open(BUNDLE_PATH).bufferedReader().use { it.readText() }
    }

    private suspend fun reset() {
        mutex.withLock {
            isolate?.close()
            sandbox?.close()
            isolate = null
            sandbox = null
        }
    }

    private fun decode(json: String): MercuryEngine.Result {
        val result = JSONObject(json)

        return MercuryEngine.Result(
            content = result.stringOrNull("content"),
            leadImageUrl = result.stringOrNull("lead_image_url"),
            error = result.optBoolean("error"),
            message = result.stringOrNull("message"),
        )
    }

    private fun JSONObject.stringOrNull(key: String): String? {
        return if (isNull(key)) null else getString(key)
    }

    companion object {
        private const val BUNDLE_PATH = "mercury/mercury-headless.js"

        private const val MAX_HEAP_SIZE_BYTES = 128L * 1024 * 1024

        private val REQUIRED_FEATURES = listOf(
            JavaScriptSandbox.JS_FEATURE_PROMISE_RETURN,
            JavaScriptSandbox.JS_FEATURE_EVALUATE_WITHOUT_TRANSACTION_LIMIT,
        )
    }
}
