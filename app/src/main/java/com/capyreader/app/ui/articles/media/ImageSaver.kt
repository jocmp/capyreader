package com.capyreader.app.ui.articles.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import coil.executeBlocking
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageSaver(
    private val context: Context,
) : KoinComponent {
    suspend fun saveImage(imageUrl: String, filename: String): Result<Uri> {

        return withContext(Dispatchers.IO) {
            try {
                val imageRequest = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
                val bitmap =
                    context.imageLoader.executeBlocking(imageRequest).drawable?.toBitmap()
                        ?: return@withContext Result.failure(Error())

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/capyreader"
                    )
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw IOException("Failed to create MediaStore entry")

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jpegStream(bitmap))
                } ?: throw IOException("Failed to open output stream")

                return@withContext Result.success(uri)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    private fun jpegStream(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}
