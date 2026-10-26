package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class StripUnlikelyCandidatesTest {
    @Test
    fun `returns original doc if no matches found`() {
        val html = """
            <div id="foo">
              <p>Ooo good one</p>
            </div>
        """
        var doc = Doc.load(html, isDocument = false)
        doc = stripUnlikelyCandidates(doc)
        assertEquals(com.jocmp.mercury.clean(html), com.jocmp.mercury.clean(doc.html()))
    }

    @Test
    fun `strips unlikely matches from the doc`() {
        val before = """
            <div class="header">Stuff</div>
            <div class="article">
              <p>Ooo good one</p>
            </div>
        """
        val after = """
            <div class="article">
              <p>Ooo good one</p>
            </div>
        """
        var doc = Doc.load(before, isDocument = false)
        doc = stripUnlikelyCandidates(doc)
        assertClean(doc.html(), after)
    }

    @Test
    fun `keeps likely matches even when they also match the blacklist`() {
        val before = """
            <div class="article adbox">
              <p>Ooo good one</p>
            </div>
        """
        val after = """
            <div class="article adbox">
              <p>Ooo good one</p>
            </div>
        """
        var doc = Doc.load(before, isDocument = false)
        doc = stripUnlikelyCandidates(doc)
        assertClean(doc.html(), after)
    }

    @Test
    fun `removed likely matches when inside blacklist node`() {
        val before = """
            <div>
              <div class="adbox">
                <div class="article">
                  <p>Ooo good one</p>
                </div>
              </div>
              <div>Something unrelated</div>
            </div>
        """
        val after = """
            <div>
              <div>Something unrelated</div>
            </div>
        """
        var doc = Doc.load(before, isDocument = false)
        doc = stripUnlikelyCandidates(doc)
        assertClean(doc.html(), after)
    }
}
