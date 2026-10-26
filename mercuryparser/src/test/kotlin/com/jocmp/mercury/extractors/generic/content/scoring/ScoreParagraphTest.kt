@file:Suppress("ktlint:standard:max-line-length")

package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreParagraphTest {
    @Test
    fun `returns 0 if text is less than 25 chars`() {
        val doc = Doc.load("<p><em>Foo</em> bar</p>")
        assertEquals(0.0, scoreParagraph(doc("p").first()))
    }

    @Test
    fun `returns 1 if text is greater than 25 chars and has 0 commas`() {
        val doc = Doc.load("<p>Lorem ipsum dolor sit amet</p>")
        assertEquals(1.0, scoreParagraph(doc("p").first()))
    }

    @Test
    fun `returns 3 if text is greater than 25 chars and has 2 commas`() {
        val doc = Doc.load("<p>Lorem ipsum, dolor sit, amet</p>")
        assertEquals(3.0, scoreParagraph(doc("p").first()))
    }

    @Test
    fun `returns 19 if text has 15 commas, ~600 chars`() {
        val doc =
            Doc.load(
                "<p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu.</p>",
            )
        assertEquals(19.0, scoreParagraph(doc("p").first()))
    }
}
