package com.capyreader.app.ui.articles.media

import androidx.compose.runtime.Composable
import com.capyreader.app.common.Media

/**
 * Content of the [com.capyreader.app.ui.Route.MediaViewer] overlay entry. [ArticleMediaView]
 * supplies the black surface, swipe-to-dismiss, and snackbar host; the fade in/out lives in
 * [MediaSceneStrategy]'s scene.
 */
@Composable
fun MediaScreen(
    media: Media,
    onDismiss: () -> Unit,
) {
    ArticleMediaView(
        onDismissRequest = onDismiss,
        media = media,
    )
}
