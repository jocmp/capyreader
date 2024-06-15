package com.jocmp.capyreader.ui.fixtures

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jocmp.capy.Article
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ArticleSample : PreviewParameterProvider<Article> {
    override val values = articles()

    private fun articles(): Sequence<Article> {
        return sequenceOf(
            Article(
                id = "288",
                feedID = "123",
                title = "How to use the Galaxy S24's AI photo editing tool",
                author = "Andrew Romero",
                contentHTML = "<div>Test</div>",
                extractedContentURL = null,
                imageURL = null,
                summary = "Test article here",
                url = URL("https://9to5google.com/?p=605559"),
                updatedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
                publishedAt = ZonedDateTime.of(2024, 3, 17, 8, 33, 0, 0, ZoneOffset.UTC),
                read = true,
                starred = false,
                feedName = "9to5Google"
            )
        )
    }
}
