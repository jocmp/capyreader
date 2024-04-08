package com.jocmp.basilreader.ui.fixtures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.RandomUUID

class FolderPreviewFixture : PreviewParameterProvider<Folder> {
    override val values = folders()

    private fun folders(): Sequence<Folder> {
        return sequenceOf(
            Folder(
                title = "Tech",
                count = 3,
                feeds = mutableListOf(
                    Feed(
                        id = RandomUUID.generate(),
                        subscriptionID = RandomUUID.generate(),
                        count = 3,
                        title = "The Verge",
                        feedURL = "https://www.theverge.com/rss/index.xml"
                    ),
                    Feed(
                        id = RandomUUID.generate(),
                        subscriptionID = RandomUUID.generate(),
                        count = 0,
                        title = "Ars Technica",
                        feedURL = "https://arstechnica.com/feed/"
                    )
                )
            ),
            Folder(
                title = "Programming",
                feeds = mutableListOf(
                    Feed(
                        id = RandomUUID.generate(),
                        subscriptionID = RandomUUID.generate(),
                        title = "Android Weekly",
                        feedURL = ""
                    ),
                    Feed(
                        id = RandomUUID.generate(),
                        subscriptionID = RandomUUID.generate(),
                        title = "Ruby Weekly",
                        feedURL = ""
                    ),
                )
            )
        )
    }
}
