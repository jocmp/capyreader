package com.jocmp.capy.articles

import com.jocmp.mercury.ContentType
import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import com.jocmp.mercury.ParseResult
import kotlinx.coroutines.CancellationException
import java.net.URL

class MercuryParser(
    private val fetchHtml: suspend (url: URL) -> Result<String>,
) {
    constructor(articleContent: ArticleContent) : this({ url -> articleContent.fetch(url) })

    suspend fun fetch(url: URL?): Result<String> {
        url ?: return Result.failure(ArticleContent.MissingURLError())

        val html = fetchHtml(url).getOrElse { return Result.failure(it) }

        val result = try {
            Mercury.parse(
                url = url.toString(),
                options = ParseOptions(
                    html = html,
                    contentType = ContentType.HTML,
                ),
            )
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

    private fun withLeadImage(result: ParseResult, content: String): String {
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
