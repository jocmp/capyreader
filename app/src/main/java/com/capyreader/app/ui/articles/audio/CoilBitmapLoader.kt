package com.capyreader.app.ui.articles.audio

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import coil3.toBitmap
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture

private const val ARTWORK_SIZE_PX = 256

@OptIn(UnstableApi::class)
class CoilBitmapLoader(private val context: Context) : BitmapLoader {
    override fun supportsMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        return load(data)
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        return load(uri.toString())
    }

    private fun load(data: Any): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()

        val request = ImageRequest.Builder(context)
            .data(data)
            .size(Size(ARTWORK_SIZE_PX, ARTWORK_SIZE_PX))
            .listener(
                onSuccess = { _, result -> future.set(result.image.toBitmap()) },
                onError = { _, result -> future.setException(result.throwable) },
                onCancel = { future.cancel(false) },
            )
            .build()

        context.imageLoader.enqueue(request)

        return future
    }
}
