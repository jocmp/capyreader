@file:Suppress("ktlint:standard:max-line-length")

package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class GetOrInitScoreTest {
    @Test
    fun `when score set - returns score if node's score already set`() {
        val doc = Doc.load("<p score=\"40\">Foo</p>")
        val score = getOrInitScore(doc("p").first(), doc)
        assertEquals(40.0, score)
    }

    @Test
    fun `returns 0 if no class-id and text less than 25 chars`() {
        val doc = Doc.load("<p>Foo</p>")
        val score = getOrInitScore(doc("p").first(), doc)
        assertEquals(0.0, score)
    }

    @Test
    fun `returns score if no class-id and has commas-length`() {
        val doc =
            Doc.load(
                "<p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu.</p>",
            )
        val score = getOrInitScore(doc("p").first(), doc)
        assertEquals(19.0, score)
    }

    @Test
    fun `returns greater score if weighted class-id is set`() {
        val doc =
            Doc.load(
                "<p class=\"entry\">Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu.</p>",
            )
        val score = getOrInitScore(doc("p").first(), doc)
        assertEquals(44.0, score)
    }

    @Test
    fun `gives 1-4 of its score to its parent`() {
        val doc =
            Doc.load(
                """
            <div>
              <p class="entry">Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu.</p>
            </div>
            """,
            )

        val node = doc("p").first()
        getOrInitScore(node, doc)
        assertEquals(16.0, getScore(node.parent()))
    }
}
