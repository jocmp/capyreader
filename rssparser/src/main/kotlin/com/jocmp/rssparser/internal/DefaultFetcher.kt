package com.jocmp.rssparser.internal

import com.jocmp.rssparser.exception.HttpException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class DefaultFetcher(
    private val callFactory: Call.Factory,
) : Fetcher {
    override suspend fun fetch(url: String): ParserInput {
        val request = createRequest(url)
        return callFactory.newCall(request).awaitForInputStream()
    }

    private fun createRequest(url: String): Request =
        Request.Builder()
            .url(url)
            .build()

    private suspend fun Call.awaitForInputStream(): ParserInput =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                cancel()
            }

            enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val body = requireNotNull(response.body)
                        val charset = response.header("content-type")?.toMediaTypeOrNull()?.charset()
                        continuation.resume(
                            ParserInput(body.bytes(), charset = charset)
                        )
                    } else {
                        val exception = HttpException(
                            code = response.code,
                            message = response.message,
                        )
                        continuation.resumeWithException(exception)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
        }
}
