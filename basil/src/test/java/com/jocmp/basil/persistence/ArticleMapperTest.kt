package com.jocmp.basil.persistence

import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

class ArticleMapperTest {
    @Test
    fun `it maps to the system's timezone`() {
        val zoneID = ZoneId.of("America/Chicago")

        val article = articleMapper(
            id = "1",
            feedID = "1",
            title = "Vizio agrees to pay $3 million for alleged ‘false’ refresh rate claims",
            contentHtml = "<p>If you bought a Vizio TV in California after April 30th, 2014, Vizio may owe you some money</p>",
            url = "https://www.theverge.com/2023/12/30/24019780/vizio-settlement-effective-refresh-rate-class-action-lawsuit",
            summary = "",
            imageURL = "https://cdn.vox-cdn.com/thumbor/r-eWiuX74LfGvTxwenExmwmkPlk=/0x0:1800x1200/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/73010063/Vizio_TV_D_Series_Lifestyle.0.jpg",
            publishedAt = 1703960809,
            arrivedAt = 1703960809,
            read = false,
            starred = false,
            zoneID = zoneID
        )

        val expectedTime = ZonedDateTime.of(
            2023,
            12,
            30,
            12,
            26,
            49,
            0,
            zoneID
        )

        assertEquals(expected = expectedTime, actual = article.arrivedAt)
    }
}
