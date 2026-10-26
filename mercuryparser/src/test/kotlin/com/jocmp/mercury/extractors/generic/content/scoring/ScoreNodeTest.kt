@file:Suppress("ktlint:standard:max-line-length")

package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreNodeTest {
    @Test
    fun `scores P like scoreParagraph - short text`() {
        val doc = Doc.load("<p><em>Foo</em> bar</p>")
        val node = doc("p").first()
        assertEquals(scoreParagraph(node), scoreNode(node))
        assertEquals(0.0, scoreNode(node))
    }

    @Test
    fun `scores P like scoreParagraph - 1`() {
        val doc = Doc.load("<p>Lorem ipsum dolor sit amet</p>")
        val node = doc("p").first()
        assertEquals(scoreParagraph(node), scoreNode(node))
        assertEquals(1.0, scoreNode(node))
    }

    @Test
    fun `scores P like scoreParagraph - 3`() {
        val doc = Doc.load("<p>Lorem ipsum, dolor sit, amet</p>")
        val node = doc("p").first()
        assertEquals(scoreParagraph(node), scoreNode(node))
        assertEquals(3.0, scoreNode(node))
    }

    @Test
    fun `scores P like scoreParagraph - 19`() {
        val doc =
            Doc.load(
                "<p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu.</p>",
            )
        val node = doc("p").first()
        assertEquals(scoreParagraph(node), scoreNode(node))
        assertEquals(19.0, scoreNode(node))
    }

    @Test
    fun `scores divs with 5`() {
        val doc = Doc.load("<div>Lorem ipsum, dolor sit, amet</div>")
        assertEquals(5.0, scoreNode(doc("div").first()))
    }

    @Test
    fun `scores the blockquote family with 3`() {
        val doc = Doc.load("<blockquote>Lorem ipsum, dolor sit, amet</blockquote>")
        assertEquals(3.0, scoreNode(doc("blockquote").first()))
    }

    @Test
    fun `scores a form with negative 3`() {
        val doc = Doc.load("<form><label>Lorem ipsum, dolor sit, amet</label></form>")
        assertEquals(-3.0, scoreNode(doc("form").first()))
    }

    @Test
    fun `scores a TH element with negative 5`() {
        val doc = Doc.load("<table><thead><tr><th>Lorem ipsum, dolor sit, amet</th></tr></thead></table>")
        assertEquals(-5.0, scoreNode(doc("th").first()))
    }
}
