package com.capyreader.app.ui.articles.reader

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun rememberProbedAspectRatio(uri: String, enabled: Boolean): Float? {
    val context = LocalContext.current
    var aspectRatio by rememberSaveable(uri) { mutableStateOf<Float?>(null) }

    LaunchedEffect(uri, enabled) {
        if (enabled && aspectRatio == null) {
            aspectRatio = probeAspectRatio(context, uri)
        }
    }

    return aspectRatio
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
