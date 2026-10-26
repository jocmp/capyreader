package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class BrsToPsTest {
    @Test
    fun `does nothing when no BRs present`() {
        val html = """
            <div id="entry">
              <p>Ooo good one</p>
            </div>
        """
        var doc = Doc.load(html, isDocument = false)
        doc = brsToPs(doc)
        assertEquals(clean(html), clean(doc.html()))
    }

    @Test
    fun `does nothing when a single BR is present`() {
        val before = """
            <div class="article adbox">
              <br>
              <p>Ooo good one</p>
            </div>
        """

        val after = """
            <div class="article adbox">
              <br>
              <p>Ooo good one</p>
            </div>
        """
        var doc = Doc.load(before, isDocument = false)
        doc = brsToPs(doc)
        assertClean(doc.html(), after)
    }

    @Test
    fun `converts double BR tags to an empty P tag`() {
        val before = """
            <div class="article adbox">
              <br />
              <br />
              <p>Ooo good one</p>
            </div>
        """

        val after = """
            <div class="article adbox">
              <p> </p><p>Ooo good one</p>
            </div>
        """
        var doc = Doc.load(before, isDocument = false)
        doc = brsToPs(doc)
        assertClean(doc.html(), after)
    }

    @Test
    fun `converts several BR tags to an empty P tag`() {
        val before = """
            <div class="article adbox">
              <br />
              <br />
              <br />
              <br />
              <br />
              <p>Ooo good one</p>
            </div>
        """

        val after = """
            <div class="article adbox">
              <p> </p><p>Ooo good one</p>
            </div>
        """
        var doc = Doc.load(before, isDocument = false)
        doc = brsToPs(doc)
        assertClean(doc.html(), after)
    }
}

private fun clean(s: String): String = com.jocmp.mercury.clean(s)
