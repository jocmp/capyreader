package com.jocmp.basilreader.ui.fixtures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jocmp.basil.Feed
import com.jocmp.basil.RandomUUID
import java.util.UUID
import kotlin.random.Random

class FeedPreviewFixture : PreviewParameterProvider<Feed> {
    override val values = feeds()

    private fun feeds(): Sequence<Feed> {
        return sequenceOf(
            Feed(
                id = RandomUUID.generate(),
                subscriptionID = RandomUUID.generate(),
                count = 10,
                name = "GamersNexus",
                feedURL = "https://gamersnexus.net/rss.xml"
            ),
            Feed(
                id = RandomUUID.generate(),
                subscriptionID = RandomUUID.generate(),
                name = "9to5Google",
                feedURL = "https://9to5google.com/feed/"
            )
        )
    }
}
