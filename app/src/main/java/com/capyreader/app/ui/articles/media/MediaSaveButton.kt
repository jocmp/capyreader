package com.capyreader.app.ui.articles.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R

@Composable
fun MediaSaveButton() {
    MediaActionButton(
        onClick = {
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
            MediaSaveButton()
        }
    }
}
