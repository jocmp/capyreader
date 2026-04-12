package com.jocmp.capy.persistence

import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.test.assertEquals

class ArticleMapperTest {
    @Test
    fun `it maps to UTC`() {
        val article = articleMapper(
            id = "1",
            feedID = "1",
            title = "Vizio agrees to pay $3 million for alleged 'false' refresh rate claims",
            author = "Wes Davis",
            contentHtml = "<p>If you bought a Vizio TV in California after April 30th, 2014, Vizio may owe you some money</p>",
            extractedContentURL = null,
            url = "https://www.theverge.com/2023/12/30/24019780/vizio-settlement-effective-refresh-rate-class-action-lawsuit",
            summary = "",
            imageURL = "https://cdn.vox-cdn.com/thumbor/r-eWiuX74LfGvTxwenExmwmkPlk=/0x0:1800x1200/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/73010063/Vizio_TV_D_Series_Lifestyle.0.jpg",
            publishedAt = 1703960809,
            enclosureType = null,
            feedTitle = "",
            faviconURL = null,
            enableStickyContent = false,
            openInBrowser = false,
            feedURL = null,
            siteURL = null,
            updatedAt = 1703960809,
            starred = false,
            read = false,
        )

        val expectedTime = ZonedDateTime.of(
            2023,
            12,
            30,
            18,
            26,
            49,
            0,
            ZoneOffset.UTC
        )

        assertEquals(expected = expectedTime, actual = article.updatedAt)
    }
}
