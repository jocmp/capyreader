package com.jocmp.mercury.resource.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class FetchResult(
    val body: ByteArray? = null,
    val response: Response? = null,
    val error: Boolean = false,
    val message: String? = null,
) {
    val failed: Boolean get() = error
}

// Evaluate a response to ensure it's something we should be keeping.
// This does not validate in the sense of a response being 200 or not.
// Validation here means that we haven't found reason to bail from
// further processing of this url.
fun validateResponse(
    response: Response,
    parseNon200: Boolean = false,
): Boolean {
    // Check if we got a valid status code
    // This isn't great, but I'm requiring a statusMessage to be set
    // before short circuiting b/c nock doesn't set it in tests
    // statusMessage only not set in nock response, in which case
    // I check statusCode, which is currently only 200 for OK responses
    // in tests
    if ((response.message.isNotEmpty() && response.message != "OK") || response.code != 200) {
        if (!parseNon200) {
            throw IllegalStateException(
                "Resource returned a response status code of ${response.code} " +
                    "and resource was instructed to reject non-200 status codes.",
            )
        }
    }

    val contentType = response.header("content-type")
    val contentLength = response.header("content-length")?.toLongOrNull() ?: 0

    // Check that the content is not in BAD_CONTENT_TYPES
    if (contentType != null && BAD_CONTENT_TYPES_RE.containsMatchIn(contentType)) {
        throw IllegalStateException(
            "Content-type for this resource was $contentType and is not allowed.",
        )
    }

    // Check that the content length is below maximum
    if (contentLength > MAX_CONTENT_LENGTH) {
        throw IllegalStateException(
            "Content for this resource was too large. Maximum content length is $MAX_CONTENT_LENGTH.",
        )
    }

    return true
}

// Grabs the last two pieces of the URL and joins them back together
// This is to get the 'livejournal.com' from 'erotictrains.livejournal.com'
fun baseDomain(uri: URI): String {
    val host = uri.host ?: return ""
    return host.split(".").takeLast(2).joinToString(".")
}

// Set our response attribute to the result of fetching our URL.
// TODO: This should gracefully handle timeouts and raise the
//       proper exceptions on the many failure cases of HTTP.
// TODO: Ensure we are not fetching something enormous. Always return
//       unicode content for HTML, with charset conversion.
suspend fun fetchResource(
    url: String,
    parsedUrl: URI? = null,
    headers: Map<String, String> = emptyMap(),
    client: OkHttpClient? = null,
): FetchResult {
    val target = (parsedUrl ?: URI.create(url)).toString()
    val httpClient = client ?: defaultClient()

    val requestBuilder = Request.Builder().url(target)
    REQUEST_HEADERS.forEach { (k, v) -> requestBuilder.header(k, v) }
    headers.forEach { (k, v) -> requestBuilder.header(k, v) }

    val response =
        try {
            executeAsync(httpClient, requestBuilder.build())
        } catch (e: IOException) {
            return FetchResult(error = true, message = e.message)
        }

    return try {
        validateResponse(response)
        val body = withContext(Dispatchers.IO) { response.body?.bytes() }
        FetchResult(body = body, response = response)
    } catch (e: Throwable) {
        response.close()
        FetchResult(error = true, message = e.message)
    }
}

private fun defaultClient(): OkHttpClient =
    OkHttpClient.Builder()
        .callTimeout(FETCH_TIMEOUT, TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

private suspend fun executeAsync(
    client: OkHttpClient,
    request: Request,
): Response =
    suspendCancellableCoroutine { cont ->
        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(
            object : okhttp3.Callback {
                override fun onFailure(
                    call: okhttp3.Call,
                    e: IOException,
                ) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(
                    call: okhttp3.Call,
                    response: Response,
                ) {
                    cont.resume(response)
                }
            },
        )
    }
