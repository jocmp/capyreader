package com.capyreader.app.ui.articles.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.ui.settings.LocalSnackbarHost
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI

@Composable
fun MediaSaveButton(imageUrl: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbarHost.current
    val successMessage = stringResource(R.string.media_save_success)
    val failureMessage = stringResource(R.string.media_save_failure)

    fun showSnackbar(message: String) {
        scope.launchUI {
            snackbar.showSnackbar(message)
        }
    }

    fun saveImage() {
        scope.launchIO {
            ImageSaver
                .saveImage(imageUrl, context = context)
                .fold(
                    onSuccess = {
                        showSnackbar(message = successMessage)
                    },
                    onFailure = {
                        showSnackbar(message = failureMessage)
                    }
                )
        }
    }

    MediaActionButton(
        onClick = {
            saveImage()
        },
        text = R.string.media_save,
        icon = Icons.Rounded.Save,
    )
}


@Preview
@Composable
fun SaveButtonPreview() {
    Box(
        Modifier.background(Color.Cyan)
    ) {
        Box(
            Modifier.background(Color.Black.copy(alpha = 0.8f)),
        ) {
            MediaSaveButton(imageUrl = "https://example.com/jpeg.jpeg")
        }
    }
}
