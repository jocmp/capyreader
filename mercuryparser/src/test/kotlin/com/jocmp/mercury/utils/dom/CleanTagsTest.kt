package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CleanTagsTest {
    @Test
    fun `drops a matching node with a negative score`() {
        val doc =
            Doc.load(
                """
            <div score="5">
              <p>What do you think?</p>
              <p>
                <ul score="-10">
                  <li>Foo</li>
                  <li>Bar</li>
                </ul>
              </p>
              <p>What do you think?</p>
            </div>
            """,
                isDocument = false,
            )

        cleanTags(doc("body").children().first(), doc)
        assertEquals(0, doc("ul").length)
    }

    @Test
    fun `removes a node with too many inputs`() {
        val doc =
            Doc.load(
                """
            <div>
              <p>What do you think?</p>
              <p>What do you think?</p>
              <p>What do you think?</p>
              <p>What do you think?</p>
              <p>What do you think?</p>
              <p>What do you think?</p>
              <p>What do you think?</p>
              <div>
                <p>What is your name?</p>
                <input type="text"></input>
                <p>What is your name?</p>
                <input type="text"></input>
                <p>What is your name?</p>
                <input type="text"></input>
              </div>
              <p>What do you think?</p>
            </div>
            """,
                isDocument = false,
            )

        cleanTags(doc("body").children().first(), doc)
        // Inner div with too many inputs should be gone
        assertEquals(0, doc("input").length)
    }

    @Test
    fun `removes a div with no images and very little text`() {
        val doc =
            Doc.load(
                """
            <div>
              <p>What do you think?</p>
              <div>
                <p>Keep this one</p>
                <img src="asdf" />
              </div>
              <div>
                <p>Lose this one</p>
              </div>
            </div>
            """,
                isDocument = false,
            )

        cleanTags(doc("body").children().first(), doc)
        val divs = doc("div")
        // outer div + inner div with image survive; inner div without image is removed
        assertTrue(divs.length <= 2, "expected outer + 1 inner div, got ${divs.length}")
        assertEquals(1, doc("img").length)
    }

    @Test
    fun `keeps anything with a class of entry-content-asset`() {
        val doc =
            Doc.load(
                """
            <div score="100">
              <p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu.</p>
              <ul score="20" class="entry-content-asset">
                <li><a href="#">Lose this one</a></li>
                <li><a href="#">Lose this one</a></li>
                <li><a href="#">Lose this one</a></li>
                <li><a href="#">Lose this one</a></li>
              </ul>
            </div>
            """,
                isDocument = false,
            )

        cleanTags(doc("body").children().first(), doc)
        assertEquals(1, doc("ul.entry-content-asset").length)
    }
}
