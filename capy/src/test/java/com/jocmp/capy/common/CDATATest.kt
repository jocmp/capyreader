package com.jocmp.capy.common

import org.junit.Test
import kotlin.test.assertEquals

class CDATATest {
    @Test
    fun `unwraps a CDATA section`() {
        assertEquals(" Hello world ", "<![CDATA[ Hello world ]]>".unwrapCDATA())
    }

    @Test
    fun `unwraps multiple CDATA sections`() {
        assertEquals("Hello world", "<![CDATA[Hello]]> <![CDATA[world]]>".unwrapCDATA())
    }

    @Test
    fun `unwraps CDATA spanning lines`() {
        assertEquals("Hello\nworld", "<![CDATA[Hello\nworld]]>".unwrapCDATA())
    }

    @Test
    fun `keeps markup inside CDATA`() {
        assertEquals("<p>Hello</p>", "<![CDATA[<p>Hello</p>]]>".unwrapCDATA())
    }

    @Test
    fun `leaves text without CDATA unchanged`() {
        assertEquals("<p>Hello world</p>", "<p>Hello world</p>".unwrapCDATA())
    }
}
