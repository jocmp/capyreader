package com.jocmp.capy.articles

import com.jocmp.capy.common.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URL

internal class ArticleContent(
    private val httpClient: OkHttpClient
) {
    suspend fun fetch(url: URL): Result<String> {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            val response = httpClient.newCall(request).await()
            val body = getBodyOrNull(response)

            if (body == null) {
                Result.failure(Error("Couldn't parse body"))
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
}
