package com.jocmp.capy.articles

import com.jocmp.capy.articles.HtmlHelpers.html
import com.jocmp.capy.testFile
import kotlin.test.Test
import kotlin.test.assertEquals

class CleanEmbedsTest {
    @Test
    fun youtubeEmbeds() {
        val document = html(
            testFile("article_partial_androidauthority.html").readText()
        )

        cleanEmbeds(document)

        HtmlHelpers.assertEquals(document) {
            """
            <div data-sanitized-class="youtube-player">
             <div class="iframe-embed" data-iframe-src="https://www.youtube.com/embed/FFLIqUhtbvI?autohide=2border=0&amp;wmode=opaque&amp;enablejsapi=1rel=0&amp;controls=1&amp;showinfo=1&amp;autoplay=1">
              <img class="iframe-embed__image" src="https://img.youtube.com/vi/FFLIqUhtbvI/hqdefault.jpg">
              <div class="iframe-embed__play-button"></div>
             </div>
            </div>
            <p>The <a href="https://www.androidauthority.com/samsung-galaxy-z-fold-6-3385438/">Samsung Z Galaxy Fold 6 </a> is a fairly unique phone. Not only is it one of the few <a href="https://www.androidauthority.com/best-foldable-phones-922793/">foldable phones</a> widely available across most major markets, but it manages to stand out from the competition thanks to<a href="https://www.androidauthority.com/samsung-s-pen-the-ultimate-guide-925944/">S Pen support</a>. While the S Pen isn’t included, we highly recommend picking it up if you want to unlock the device’s full potential.</p>
            <p>Of course, the S Pen and large screen won’t help you much if you don’t have the right apps to pair with it. Thinking about pre-ordering a Galaxy Fold 6? Let’s take a closer look at the best apps you can download on your new device once you get it, all of which take advantage of the phone’s large screen and S Pen functionality.</p>
            """.trimIndent()
        }
    }

    @Test
    fun `youtube match`() {
        val result =
            findYouTubeMatch("https://www.youtube.com/embed/FFLIqUhtbvI?autoplay=0&amp;autohide=2border=0&amp;wmode=opaque&amp;enablejsapi=1rel=0&amp;controls=1&amp;showinfo=1")

        assertEquals(expected = "FFLIqUhtbvI", actual = result)
    }

    @Test
    fun `youtube nocookie match`() {
        val result = findYouTubeMatch("https://www.youtube-nocookie.com/embed/mqjMWSnwnTM")

        assertEquals(expected = "mqjMWSnwnTM", actual = result)
    }

    @Test
    fun `youtu be match`() {
        val result = findYouTubeMatch("https://youtu.be/mqjMWSnwnTM")

        assertEquals(expected = "mqjMWSnwnTM", actual = result)
    }
}
