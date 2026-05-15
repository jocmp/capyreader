package com.jocmp.rssparser.internal

import com.jocmp.rssparser.exception.HttpException
import com.jocmp.rssparser.exception.NonFeedResponseException
import com.jocmp.rssparser.model.ConditionalGetInfo
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
    override suspend fun fetch(url: String, conditionalGet: ConditionalGetInfo): FetchResponse {
        val request = createRequest(url, conditionalGet)
        return callFactory.newCall(request).awaitForResponse()
    }

    private fun createRequest(url: String, conditionalGet: ConditionalGetInfo): Request {
        val builder = Request.Builder().url(url)
        conditionalGet.etag?.takeIf { it.isNotBlank() }?.let {
            builder.header("If-None-Match", it)
        }
        conditionalGet.lastModified?.takeIf { it.isNotBlank() }?.let {
            builder.header("If-Modified-Since", it)
        }
        return builder.build()
    }

    private suspend fun Call.awaitForResponse(): FetchResponse =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                cancel()
            }

            enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 304) {
                        response.close()
                        continuation.resume(
                            FetchResponse(parserInput = null, conditionalGet = ConditionalGetInfo.EMPTY)
                        )
                        return
                    }

                    if (!response.isSuccessful) {
                        continuation.resumeWithException(
                            HttpException(code = response.code, message = response.message)
                        )
                        return
                    }

                    val body = requireNotNull(response.body)
                    val source = body.source()

                    if (source.isDefinitelyNotFeed()) {
                        response.close()
                        continuation.resumeWithException(
                            NonFeedResponseException(call.request().url.toString(), "image")
                        )
                        return
                    }

                    val charset = response.header("content-type")?.toMediaTypeOrNull()?.charset()
                    val parserInput = ParserInput(body.bytes(), charset = charset)
                    val responseConditionalGet = ConditionalGetInfo(
                        etag = response.header("ETag"),
                        lastModified = response.header("Last-Modified"),
                    )
                    continuation.resume(
                        FetchResponse(parserInput = parserInput, conditionalGet = responseConditionalGet)
                    )
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
        }
}

