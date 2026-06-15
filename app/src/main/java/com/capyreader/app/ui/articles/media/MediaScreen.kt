package com.capyreader.app.ui.articles.media

import androidx.compose.runtime.Composable
import com.capyreader.app.common.Media

/**
 * Content of the [com.capyreader.app.ui.Route.MediaViewer] overlay entry. [ArticleMediaView] /
 * [MediaScaffold] already supply the black full-screen surface, swipe-to-dismiss, and their own
 * snackbar host, so this is just the entry seam (and the home for the open/close transition).
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
