package com.jocmp.basilreader.ui.fixtures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import java.util.UUID

class FolderPreviewFixture : PreviewParameterProvider<Folder> {
    override val values = folders()

    private fun folders(): Sequence<Folder> {
        return sequenceOf(
            Folder(
                title = "Tech",
                feeds = mutableListOf(
                    Feed(id = UUID.randomUUID().toString(), name = "The Verge", feedURL = "https://www.theverge.com/rss/index.xml"),
                    Feed(id = UUID.randomUUID().toString(), name = "Ars Technica", feedURL = "https://arstechnica.com/feed/")
                )
            ),
            Folder(
                title = "Programming",
                feeds = mutableListOf(
                    Feed(id = UUID.randomUUID().toString(), name = "Android Weekly", feedURL = ""),
                    Feed(id = UUID.randomUUID().toString(), name = "Ruby Weekly", feedURL = ""),
                )
            )
        )
    }
}
