package com.jocmp.capy.accounts.reader

import com.jocmp.readerclient.Item
import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderEnclosureParsingTest {
    @Test
    fun validEnclosures() {
        val href =
            "https://cdn.arstechnica.net/wp-content/uploads/2022/09/GettyImages-681547496-1152x648.jpg"

        val enclosures = listOf(
            Item.Enclosure(
                href = href,
                type = "image/jpeg"
            ),
            Item.Enclosure(
                href = "invalid_url",
                type = "image/jpeg"
            ),
            Item.Enclosure(
                href = null,
                type = null
            )
        )

        val item = ItemFixtures.item.copy(enclosure = enclosures)

        val results = ReaderEnclosureParsing.validEnclosures(item)
        assertEquals(expected = 1, actual = results.size)
        val enclosure = results.first()

        assertEquals(expected = href, actual = enclosure.url.toString())
        assertEquals(expected = "image/jpeg", actual = enclosure.type)
    }
}
