package com.jocmp.capy.articles

import com.jocmp.capy.BrowserHeadersInterceptor
import com.jocmp.capy.common.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URL

class ArticleContent(
    client: OkHttpClient,
    userAgent: String,
    acceptLanguage: String,
) {
    private val httpClient =
        client.newBuilder().addInterceptor(BrowserHeadersInterceptor(userAgent, acceptLanguage)).build()

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

    private fun getBodyOrNull(response: Response): String? {
        val body = response.body
        val bodyValue = body.string()

        return if (
            bodyValue.isNotBlank() &&
            body.contentType()?.subtype == "html"
        ) {
            bodyValue
        } else {
            null
        }
    }

    class MissingBodyError(override val message: String?) : Throwable(message = message)

    class MissingURLError : Throwable()

    class HttpError(val code: Int) : Throwable()
}
