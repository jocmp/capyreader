package com.capyreader.app.ui.articles.reader

import com.capyreader.app.ui.components.ShareLink
import com.jocmp.mallet.LinearImage

class ReaderActions(
    val onLinkClick: (url: String, elementIndex: Int?) -> Unit,
    val onLinkLongPress: (link: ShareLink) -> Unit,
    val onImageClick: (image: LinearImage) -> Unit,
    val onImageLongPress: (url: String) -> Unit,
    val onAudioClick: (url: String) -> Unit,
) {
    companion object {
        val none = ReaderActions(
            onLinkClick = { _, _ -> },
            onLinkLongPress = {},
            onImageClick = {},
            onImageLongPress = {},
            onAudioClick = {},
        )
    }
}
