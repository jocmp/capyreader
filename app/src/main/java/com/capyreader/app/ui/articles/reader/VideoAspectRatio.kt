package com.capyreader.app.ui.articles.reader

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateMapOf
import coil3.imageLoader
import coil3.request.ImageRequest
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.inspector.MetadataRetriever
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.jocmp.capy.accounts.baseHttpClient
import com.jocmp.capy.logging.CapyLog
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine

private const val MIN_VIDEO_ASPECT_RATIO = 3f / 4f

@OptIn(UnstableApi::class)
fun videoMediaSourceFactory(context: Context): DefaultMediaSourceFactory {
    return DefaultMediaSourceFactory(context)
        .setDataSourceFactory(OkHttpDataSource.Factory(baseHttpClient()))
}

class VideoAspectRatios(private val context: Context) {
    private val ratios = mutableStateMapOf<String, Float>()

    private val probed = mutableSetOf<String>()

    operator fun get(uri: String): Float? = ratios[uri]

    private fun putPoster(uri: String, width: Float, height: Float) {
        if (uri in probed) {
            return
        }

        videoAspectRatio(width, height)?.let { ratios[uri] = it }
    }

    suspend fun measurePoster(uri: String, poster: String) {
        if (uri in ratios) {
            return
        }

        val request = ImageRequest.Builder(context)
            .data(poster)
            .build()

        val image = context.imageLoader.execute(request).image ?: return

        putPoster(uri, image.width.toFloat(), image.height.toFloat())
    }

    suspend fun probe(uri: String) {
        if (uri in probed) {
            return
        }

        probeAspectRatio(context, uri)?.let {
            probed.add(uri)
            ratios[uri] = it
        }
    }
}

fun videoAspectRatio(width: Float?, height: Float?): Float? {
    if (width == null || height == null || width <= 0f || height <= 0f) {
        return null
    }

    return (width / height).coerceAtLeast(MIN_VIDEO_ASPECT_RATIO)
}

@OptIn(UnstableApi::class)
private suspend fun probeAspectRatio(context: Context, uri: String): Float? {
    val retriever = MetadataRetriever.Builder(context, MediaItem.fromUri(uri))
        .setMediaSourceFactory(videoMediaSourceFactory(context))
        .build()

    return try {
        retriever.use { it.retrieveTrackGroups().await() }.videoFormat()?.aspectRatio()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        CapyLog.warn("probe_video", mapOf("error" to e.message))
        null
    }
}

@OptIn(UnstableApi::class)
private fun TrackGroupArray.videoFormat(): Format? {
    return (0 until length)
        .map { get(it) }
        .firstOrNull { it.type == C.TRACK_TYPE_VIDEO }
        ?.getFormat(0)
}

private fun Format.aspectRatio(): Float? {
    val displayWidth = width * pixelWidthHeightRatio
    val displayHeight = height.toFloat()

    if (rotationDegrees == 90 || rotationDegrees == 270) {
        return videoAspectRatio(displayHeight, displayWidth)
    }

    return videoAspectRatio(displayWidth, displayHeight)
}

private suspend fun <T> ListenableFuture<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addListener(
            {
                try {
                    continuation.resume(get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e.cause ?: e)
                }
            },
            MoreExecutors.directExecutor(),
        )

        continuation.invokeOnCancellation { cancel(true) }
    }
}
