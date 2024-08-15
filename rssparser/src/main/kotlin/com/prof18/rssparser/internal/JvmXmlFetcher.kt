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

internal class JvmXmlFetcher(
    private val callFactory: Call.Factory,
) : XmlFetcher {
    override suspend fun fetchXml(url: String): ParserInput {
        val request = createRequest(url)
        return ParserInput(
            inputStream = callFactory.newCall(request).awaitForInputStream()
        )
    }

    override suspend fun fetchXmlAsString(url: String): String {
        val request = createRequest(url)
        return callFactory.newCall(request).awaitForString()
    }

    private fun createRequest(url: String): Request =
        Request.Builder()
            .url(url)
            .build()

    private suspend fun Call.awaitForInputStream(): InputStream = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }

        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = requireNotNull(response.body)
                    continuation.resume(body.byteStream())
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

    private suspend fun Call.awaitForString(): String = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }

        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = requireNotNull(response.body)
                    continuation.resume(body.string())
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
