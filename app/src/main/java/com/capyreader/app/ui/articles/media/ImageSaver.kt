package com.capyreader.app.ui.articles.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.capyreader.app.common.MD5
import com.capyreader.app.common.externalImageCacheFile
import com.capyreader.app.common.fileURI
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException

object ImageSaver {
    suspend fun saveImage(imageUrl: String, context: Context): Result<Uri> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val bitmap =
                    createBitmap(imageUrl, context) ?: throw IOException("Failed to generate image")

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, jpegFileName(imageUrl))
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/Capy Reader"
                    )
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw IOException("Failed to create MediaStore entry")

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jpegStream(bitmap))
                } ?: throw IOException("Failed to open output stream")

                Result.success(uri)
            } catch (e: Exception) {
                CapyLog.error("save_img", error = e)
                Result.failure(e)
            }
        }
    }

    suspend fun shareImage(imageUrl: String, context: Context): Result<Uri> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val bitmap =
                    createBitmap(imageUrl, context) ?: throw IOException("Failed to generate image")

                val target = context.externalImageCacheFile(jpegFileName(imageUrl))

                context.contentResolver.openFileDescriptor(target.toUri(), "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use {
                        it.write(jpegStream(bitmap))
                    }
                }

                Result.success(context.fileURI(target))
            } catch (e: Exception) {
                CapyLog.error("share_img", error = e)
                Result.failure(e)
            }
        }
    }

    private fun jpegStream(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun jpegFileName(imageUrl: String): String {
        return "${MD5.from(imageUrl)}.jpg"
    }

    private suspend fun createBitmap(imageUrl: String, context: Context): Bitmap? {
        val imageRequest = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        return context.imageLoader.execute(imageRequest).image?.toBitmap()
    }
}
