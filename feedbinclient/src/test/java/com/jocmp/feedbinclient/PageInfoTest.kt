package com.jocmp.feedbinclient

import org.junit.Assert.assertEquals
import org.junit.Test

class PagingInfoTest {
    @Test
    fun it_parses_a_header() {
        val linkHeader = "<https://api.feedbin.com/v2/entries.json?page=2>; rel=\"next\", <https://api.feedbin.com/v2/entries.json?page=51>; rel=\"last\""
        val result = PagingInfo.fromHeader(linkHeader)

        assertEquals(PagingInfo(nextPage = 2, lastPage = 51), result)
    }
}
