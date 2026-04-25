package com.jocmp.rssparser.internal

import okio.Buffer
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BufferedSourceExtTest {
    @Test
    fun `detects PNG`() {
        assertTrue(source("89504e470d0a1a0a".decodeHex()).isDefinitelyNotFeed())
    }

    @Test
    fun `detects JPEG`() {
        assertTrue(source("ffd8ffe000104a464946".decodeHex()).isDefinitelyNotFeed())
    }

    @Test
    fun `detects GIF`() {
        assertTrue(source("474946383961".decodeHex()).isDefinitelyNotFeed())
    }

    @Test
    fun `detects PDF`() {
        assertTrue(source("255044462d312e34".decodeHex()).isDefinitelyNotFeed())
    }

    @Test
    fun `detects ZIP`() {
        assertTrue(source("504b03040a000000".decodeHex()).isDefinitelyNotFeed())
    }

    @Test
    fun `detects GZIP`() {
        assertTrue(source("1f8b0808000000".decodeHex()).isDefinitelyNotFeed())
    }

    @Test
    fun `detects MP3 with ID3 header`() {
        assertTrue(source("4944330300000000".decodeHex()).isDefinitelyNotFeed())
    }

    @Test
    fun `passes XML`() {
        assertFalse(source("<?xml version=\"1.0\"?>".encodeUtf8()).isDefinitelyNotFeed())
    }

    @Test
    fun `passes HTML`() {
        assertFalse(source("<!DOCTYPE html><html>".encodeUtf8()).isDefinitelyNotFeed())
    }

    @Test
    fun `passes JSON feed`() {
        assertFalse(source("{\"version\":\"https:".encodeUtf8()).isDefinitelyNotFeed())
    }

    @Test
    fun `passes empty body`() {
        assertFalse(source("".encodeUtf8()).isDefinitelyNotFeed())
    }

    @Test
    fun `does not consume bytes`() {
        val buffer = Buffer().write("<?xml version=\"1.0\"?>".encodeUtf8())
        buffer.isDefinitelyNotFeed()
        assertTrue(buffer.readUtf8().startsWith("<?xml"))
    }

    private fun source(bytes: okio.ByteString): Buffer = Buffer().write(bytes)
}
