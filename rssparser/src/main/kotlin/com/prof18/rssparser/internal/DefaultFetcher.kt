package com.prof18.rssparser.internal

import com.prof18.rssparser.exception.HttpException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
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
                        continuation.resume(
                            ParserInput(body.bytes())
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
