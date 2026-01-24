package com.jocmp.capy.articles

import com.jocmp.capy.UserAgentInterceptor
import com.jocmp.capy.common.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URL

class ArticleContent(
    client: OkHttpClient
) {
    private val httpClient =
        client.newBuilder().addInterceptor(UserAgentInterceptor(USER_AGENT)).build()

    internal suspend fun fetch(url: URL?): Result<String> {
        url ?: return Result.failure(MissingURLError())

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            val response = httpClient.newCall(request).await()

            if (!response.isSuccessful) {
                return Result.failure(HttpError(code = response.code))
            }

            val body = getBodyOrNull(response)

            if (body == null) {
                Result.failure(MissingBodyError(message = response.code.toString()))
            } else {
                Result.success(body)
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Passes charset from Content-Type header if available, otherwise lets Jsoup
    // auto-detect from HTML <meta> tags (e.g., <meta charset="ISO-8859-1">)
    private fun getBodyOrNull(response: Response): String? {
        val body = response.body
        val contentType = body.contentType()

        if (contentType?.subtype != "html") {
            return null
        }

        val bytes = body.bytes()

        if (bytes.isEmpty()) {
            return null
        }

        val charset = contentType.charset()?.name()
        val baseUri = response.request.url.toString()
        val document = Jsoup.parse(bytes.inputStream(), charset, baseUri)

        return document.html()
    }

    class MissingBodyError(override val message: String?) : Throwable(message = message)

    class MissingURLError : Throwable()

    class HttpError(val code: Int) : Throwable()

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"
    }
}
