package com.jocmp.feedbinclient.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PagingInfoTest {
    @Test
    fun it_parses_a_header() {
        val linkHeader = "<https://api.feedbin.com/v2/entries.json?page=2>; rel=\"next\", <https://api.feedbin.com/v2/entries.json?page=51>; rel=\"last\""
        val result = PagingInfo.fromHeader(linkHeader)

        assertEquals(PagingInfo(nextPage = 2, lastPage = 51), result)
    }
}
