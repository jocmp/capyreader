package com.jocmp.capy.articles

import com.jocmp.capy.UserAgentInterceptor
import com.jocmp.capy.common.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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
        val bodyValue = body?.string().orEmpty()

        return if (
            response.isSuccessful &&
            body != null &&
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

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"
    }
}
