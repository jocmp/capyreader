package com.jocmp.feedfinder

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class DefaultRequest(private val client: OkHttpClient = OkHttpClient()) : Request {
    override suspend fun fetch(url: URL): Response {
        val parsedURL = URL(url.protocol, url.host, url.port, url.file)

        val request = okhttp3.Request.Builder()
            .url(parsedURL)
            .get()
            .build()

        val response = client.newCall(request).await()
        val body = response.body?.string().orEmpty()

        return Response(
            url = parsedURL,
            body = body,
            charset = response.header("content-type")?.toMediaType()?.charset()
        )
    }
}

internal suspend inline fun Call.await(): okhttp3.Response {
    return suspendCancellableCoroutine { continuation ->
        val callback = ContinuationCallback(this, continuation)
        enqueue(callback)
        continuation.invokeOnCancellation(callback)
    }
}

internal class ContinuationCallback(
    private val call: Call,
    private val continuation: CancellableContinuation<okhttp3.Response>
) : Callback, CompletionHandler {

    override fun onFailure(call: Call, e: IOException) {
        if (!call.isCanceled()) {
            continuation.resumeWithException(e)
        }
    }

    override fun onResponse(call: Call, response: okhttp3.Response) {
        continuation.resume(response)
    }

    override fun invoke(cause: Throwable?) {
        try {
            call.cancel()
        } catch (_: Throwable) {
        }
    }
}
