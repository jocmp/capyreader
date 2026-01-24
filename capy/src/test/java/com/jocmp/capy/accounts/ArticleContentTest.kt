package com.jocmp.capy.accounts

import com.jocmp.capy.articles.ArticleContent
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mock.ClasspathResources.resource
import okhttp3.mock.MediaTypes.MEDIATYPE_HTML
import okhttp3.mock.MockInterceptor
import okhttp3.mock.eq
import okhttp3.mock.get
import okhttp3.mock.rule
import okhttp3.mock.url
import org.junit.Before
import org.junit.Test
import java.net.URL
import kotlin.test.assertContains


class ArticleContentTest {
    private val theVergeArticleURL =
        "https://www.theverge.com/2024/7/11/24195947/sonos-lasso-soundbar-photos-features-leak"
    private val arsTechnicaURL =
        "https://arstechnica.com/gadgets/2024/07/three-betas-in-ios-18-testers-still-cant-try-out-apple-intelligence-features/"

    private val interceptor = MockInterceptor().apply {
        rule(get, url eq theVergeArticleURL) {
            respond(resource("article_the_verge.html"), MEDIATYPE_HTML)
        }

        rule(get, url eq arsTechnicaURL) {
            respond(resource("article_ars_technica.html"), MEDIATYPE_HTML)
        }
    }
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()
    private lateinit var extractor: ArticleContent

    @Before
    fun setup() {
        extractor = ArticleContent(
            client = client,
            userAgent = "TestUserAgent",
            acceptLanguage = "en-US",
        )
    }

    @Test
    fun extractContent() = runTest {
        val result = extractor.fetch(url = URL(theVergeArticleURL)).getOrThrow()

        assertContains(result, "Do you know more about upcoming Sonos products?")
    }

    @Test
    fun extractContent_UnsuccessfulResponse() {
    }


    @Test
    fun extractContent_Exception() { // Valid?
    }

    @Test
    fun extractContent_EmptyBody() {
    }

    @Test
    fun extractContent_ParseError() {
    }
}
