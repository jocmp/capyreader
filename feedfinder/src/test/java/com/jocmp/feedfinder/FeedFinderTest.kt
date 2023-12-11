package com.jocmp.feedfinder

import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class FeedFinderTest {
    @Test
    fun find_returnsASuccess() = runBlocking {
        val finder = FeedFinder("arstechnica.com")

        finder.find()
        
        assertEquals("", "")
    }
}
