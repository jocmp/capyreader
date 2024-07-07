package com.capyreader.app.ui.fixtures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jocmp.capy.Feed
import com.jocmp.capy.RandomUUID

class FeedPreviewFixture : PreviewParameterProvider<Feed> {
    override val values = feeds()

    private fun feeds(): Sequence<Feed> {
        return sequenceOf(
            Feed(
                id = RandomUUID.generate(),
                subscriptionID = RandomUUID.generate(),
                count = 10,
                title = "GamersNexus",
                feedURL = "https://gamersnexus.net/rss.xml"
            ),
            Feed(
                id = RandomUUID.generate(),
                subscriptionID = RandomUUID.generate(),
                title = "9to5Google",
                feedURL = "https://9to5google.com/feed/"
            )
        )
    }
}
