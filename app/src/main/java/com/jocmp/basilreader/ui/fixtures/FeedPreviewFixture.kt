package com.jocmp.basilreader.ui.fixtures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jocmp.basil.Feed
import java.util.UUID

class FeedPreviewFixture : PreviewParameterProvider<Feed> {
    override val values = feeds()

    private fun feeds(): Sequence<Feed> {
        return sequenceOf(
            Feed(id = UUID.randomUUID().toString(), name = "GamersNexus", feedURL = "https://gamersnexus.net/rss.xml"),
            Feed(id = UUID.randomUUID().toString(), name = "9to5Google", feedURL = "https://9to5google.com/feed/")
        )
    }
}
