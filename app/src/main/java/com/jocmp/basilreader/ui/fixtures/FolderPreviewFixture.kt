package com.jocmp.basilreader.ui.fixtures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import java.util.UUID

class FolderPreviewFixture : PreviewParameterProvider<Folder> {
    override val values = folders()

    private fun folders(): Sequence<Folder> {
        return sequenceOf(
            Folder(title = "Empty Folder"),
            Folder(
                title = "Tech",
                feeds = mutableListOf(
                    Feed(id = UUID.randomUUID().toString(), name = "The Verge"),
                    Feed(id = UUID.randomUUID().toString(), name = "Ars Technica")
                )
            ),
            Folder(
                title = "Programming",
                feeds = mutableListOf(
                    Feed(id = UUID.randomUUID().toString(), name = "Android Weekly"),
                    Feed(id = UUID.randomUUID().toString(), name = "Ruby Weekly"),
                )
            )
        )
    }
}
