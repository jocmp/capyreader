package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.assertClean
import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test

class ConvertToParagraphsTest {
    @Test
    fun `does not convert a div with nested p children`() {
        val html = """
            <div>
              <div>
                <div>
                  <p>This is a paragraph</p>
                </div>
              </div>
            </div>
        """
        var doc = Doc.load(html, isDocument = false)
        doc = convertToParagraphs(doc)
        assertClean(doc("body").html(), html)
    }
}
