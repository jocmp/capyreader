package com.jocmp.mercury.resource.utils.dom

import com.jocmp.mercury.dsl.Doc
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertLazyLoadedImagesTest {
    @Test
    fun `moves image links to src if placed in another attribute`() {
        val doc = Doc.load("<img data-src=\"http://example.com/foo.jpg\">")
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals(
            "<img data-src=\"http://example.com/foo.jpg\" src=\"http://example.com/foo.jpg\">",
            result,
        )
    }

    @Test
    fun `moves image source candidates to srcset if placed in another attribute`() {
        val doc = Doc.load("<img data-srcset=\"http://example.com/foo.jpg 2x\">")
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals(
            "<img data-srcset=\"http://example.com/foo.jpg 2x\" srcset=\"http://example.com/foo.jpg 2x\">",
            result,
        )
    }

    @Test
    fun `moves image source candidates containing query strings to srcset if placed in another attribute`() {
        val doc =
            Doc.load(
                "<img data-srcset=\"http://example.com/foo.jpg?w=400 2x, http://example.com/foo.jpg?w=600 3x\">",
            )
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals(
            "<img data-srcset=\"http://example.com/foo.jpg?w=400 2x, http://example.com/foo.jpg?w=600 3x\" " +
                "srcset=\"http://example.com/foo.jpg?w=400 2x, http://example.com/foo.jpg?w=600 3x\">",
            result,
        )
    }

    @Test
    fun `properly handles src and srcset attributes`() {
        val doc =
            Doc.load(
                "<img data-src=\"http://example.com/foo.jpg\" data-srcset=\"http://example.com/foo.jpg 2x\">",
            )
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals(
            "<img data-src=\"http://example.com/foo.jpg\" data-srcset=\"http://example.com/foo.jpg 2x\" " +
                "src=\"http://example.com/foo.jpg\" srcset=\"http://example.com/foo.jpg 2x\">",
            result,
        )
    }

    @Test
    fun `does nothing when value is not a link`() {
        // This is far from perfect, since a relative url could
        // be perfectly correct.
        val doc = Doc.load("<img data-src=\"foo.jpg\">")
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals("<img data-src=\"foo.jpg\">", result)
    }

    @Test
    fun `does nothing when value is not an image`() {
        val doc = Doc.load("<img data-src=\"http://example.com\">")
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals("<img data-src=\"http://example.com\">", result)
    }

    @Test
    fun `does not change a correct img with src`() {
        val doc = Doc.load("<img src=\"http://example.com/foo.jpg\">")
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals("<img src=\"http://example.com/foo.jpg\">", result)
    }

    @Test
    fun `does not replace an img src with srcset value`() {
        val doc =
            Doc.load(
                "<img src=\"http://example.com/foo.jpg\" " +
                    "srcset=\"http://example.com/foo2x.jpg 2x, http://example.com/foo.jpg\">",
            )
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals(
            "<img src=\"http://example.com/foo.jpg\" " +
                "srcset=\"http://example.com/foo2x.jpg 2x, http://example.com/foo.jpg\">",
            result,
        )
    }

    @Test
    fun `does not replace an img src with an unexpanded URL template`() {
        val html =
            "<img src=\"http://example.com/resize/1100/?url=foo.jpg\" " +
                "data-template=\"http://example.com/resize/{width}/?url=foo.jpg\">"
        val doc = Doc.load(html)
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals(html, result)
    }

    @Test
    fun `does not replace an img src or srcset with a percent-encoded URL template`() {
        val html =
            "<img src=\"http://example.com/foo.jpg\" " +
                "data-template=\"http://example.com/resize/%7Bwidth%7D/foo.jpg\" " +
                "data-srcset=\"http://example.com/resize/%7Bwidth%7D/foo.jpg 2x\">"
        val doc = Doc.load(html)
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals(html, result)
    }

    @Test
    fun `moves a URL template to src when there is no usable src`() {
        val doc = Doc.load("<img data-pattern=\"http://example.com/foo-{{size}}.jpg\">")
        val result = convertLazyLoadedImages(doc)("body").html()

        assertEquals(
            "<img data-pattern=\"http://example.com/foo-{{size}}.jpg\" src=\"http://example.com/foo-{{size}}.jpg\">",
            result,
        )
    }
}
