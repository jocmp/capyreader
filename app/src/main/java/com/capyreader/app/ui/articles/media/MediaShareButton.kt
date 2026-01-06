package com.capyreader.app.ui.articles.media

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.common.shareImage
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
                        context.shareImage(uri)
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
