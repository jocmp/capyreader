package com.jocmp.basilreader.ui.fixtures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jocmp.basil.Account
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.RandomUUID
import java.util.UUID
import kotlin.random.Random

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
                        externalID = RandomUUID.generate(),
                        count = 3,
                        name = "The Verge",
                        feedURL = "https://www.theverge.com/rss/index.xml"
                    ),
                    Feed(
                        id = RandomUUID.generate(),
                        externalID = RandomUUID.generate(),
                        count = 0,
                        name = "Ars Technica",
                        feedURL = "https://arstechnica.com/feed/"
                    )
                )
            ),
            Folder(
                title = "Programming",
                feeds = mutableListOf(
                    Feed(
                        id = RandomUUID.generate(),
                        externalID = RandomUUID.generate(),
                        name = "Android Weekly",
                        feedURL = ""
                    ),
                    Feed(
                        id = RandomUUID.generate(),
                        externalID = RandomUUID.generate(),
                        name = "Ruby Weekly",
                        feedURL = ""
                    ),
                )
            )
        )
    }
}
