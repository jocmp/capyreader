package com.capyreader.app.ui.articles.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.settings.LocalSnackbarHost
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI
import com.jocmp.capy.common.withUIContext

@Composable
fun MediaShareButton(imageUrl: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val failureMessage = stringResource(R.string.media_share_failure)
    val snackbar = LocalSnackbarHost.current

    fun showFailureMessage() {
        scope.launchUI {
            snackbar.showSnackbar(failureMessage)
        }
    }

    fun shareImage() {
        scope.launchIO {
            val result = ImageSaver.shareImage(imageUrl, context = context)

            withUIContext {
                result.fold(
                    onSuccess = { uri ->
                        openShareSheet(uri, context = context)
                    },
                    onFailure = {
                        showFailureMessage()
                    }
                )
            }
        }
    }

    MediaActionButton(
        onClick = {
            shareImage()
        },
        text = R.string.media_share,
        icon = Icons.Rounded.Share,
    )
}

private fun openShareSheet(uri: Uri, context: Context) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, uri)
        setDataAndType(uri, "image/jpeg")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    context.startActivity(Intent.createChooser(shareIntent, null))
}
