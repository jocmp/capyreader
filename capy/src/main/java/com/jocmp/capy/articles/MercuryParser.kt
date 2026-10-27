package com.jocmp.capy.articles

import kotlinx.coroutines.CancellationException
import java.net.URL

class MercuryParser(
    private val engine: MercuryEngine = KotlinMercuryEngine,
    private val fetchHtml: suspend (url: URL) -> Result<String>,
) {
    constructor(
        articleContent: ArticleContent,
        engine: MercuryEngine = KotlinMercuryEngine,
    ) : this(engine, { url -> articleContent.fetch(url) })

    suspend fun fetch(url: URL?): Result<String> {
        url ?: return Result.failure(ArticleContent.MissingURLError())

        val html = fetchHtml(url).getOrElse { return Result.failure(it) }

        val result = try {
            engine.parse(url = url.toString(), html = html)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(ParseError(message = e.message, cause = e))
        }

        val content = result.content

        if (result.error || content.isNullOrBlank()) {
            return Result.failure(ParseError(message = result.message))
        }

        return Result.success(withLeadImage(result, content))
    }

    private fun withLeadImage(result: MercuryEngine.Result, content: String): String {
        val leadImage = result.leadImageUrl

        if (leadImage.isNullOrBlank() || IMG_TAG.containsMatchIn(content)) {
            return content
        }

        return """<img src="${escapeAttribute(leadImage)}">""" + content
    }

    private fun escapeAttribute(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
    }

    class ParseError(
        override val message: String?,
        override val cause: Throwable? = null,
    ) : Throwable(message = message, cause = cause)

    companion object {
        private val IMG_TAG = Regex("<img[\\s>]", RegexOption.IGNORE_CASE)
    }
}
