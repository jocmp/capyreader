package com.jocmp.capy.articles

import com.jocmp.mercury.ContentType
import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import com.jocmp.mercury.ParseResult
import okhttp3.OkHttpClient
import java.net.URL

class MercuryParser(
    private val httpClient: OkHttpClient,
    userAgent: String,
    acceptLanguage: String,
) {
    private val headers: Map<String, String> = mapOf(
        "User-Agent" to userAgent,
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
        "Accept-Language" to acceptLanguage,
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "none",
        "Sec-Fetch-User" to "?1",
        "Upgrade-Insecure-Requests" to "1",
    )

    suspend fun fetch(url: URL?): Result<String> {
        url ?: return Result.failure(MissingURLError())

        val result = Mercury.parse(
            url = url.toString(),
            options = ParseOptions(
                contentType = ContentType.HTML,
                headers = headers,
                httpClient = httpClient,
            ),
        )

        if (result.error || result.content == null) {
            return Result.failure(ParseError(message = result.message))
        }

        return Result.success(buildContent(result))
    }

    private fun buildContent(result: ParseResult): String {
        val body = result.content.orEmpty()
        val leadImage = result.leadImageUrl

        if (leadImage.isNullOrBlank() || hasImage(body)) {
            return body
        }

        return """<img src="${escapeAttr(leadImage)}" />""" + body
    }

    private fun hasImage(html: String): Boolean = IMG_TAG.containsMatchIn(html)

    private fun escapeAttr(value: String): String =
        value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;")

    class ParseError(override val message: String?) : Throwable(message = message)

    class MissingURLError : Throwable()

    companion object {
        private val IMG_TAG = Regex("<img[\\s>]", RegexOption.IGNORE_CASE)
    }
}
