package com.jocmp.capy.offline

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.articles.MercuryParser
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.articleMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mock.Behavior
import okhttp3.mock.MockInterceptor
import okhttp3.mock.eq
import okhttp3.mock.get
import okhttp3.mock.rule
import okhttp3.mock.url
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OfflineDownloaderTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var database: Database
    private lateinit var feedFixture: FeedFixture
    private lateinit var articleFixture: ArticleFixture

    private val articleUrl = "https://example.com/articles/post-1"
    private val absoluteImageUrl = "https://cdn.example.com/hero.jpg"
    private val relativeImagePath = "/img/inline.png"
    private val resolvedRelativeImageUrl = "https://example.com/img/inline.png"

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        feedFixture = FeedFixture(database)
        articleFixture = ArticleFixture(database)
    }

    @Test
    fun run_absolutizesAndCachesMediaUrls() = runTest {
        val feed = feedFixture.create(feedURL = "https://example.com/feed")
        runBlocking {
            FeedRecords(database).updateCacheOffline(feed.id, enabled = true)
        }
        val article = articleFixture.create(feed = feed, url = articleUrl)

        val mercuryHtml = """
            <p>This is a long enough paragraph to look like real article content for testing purposes.</p>
            <img src="$absoluteImageUrl" alt="hero">
            <p>More body text follows here so the parseable-body heuristic accepts it.</p>
            <img src="$relativeImagePath" alt="inline">
        """.trimIndent()

        val mercury = mockk<MercuryParser>()
        coEvery { mercury.fetch(any()) } returns Result.success(mercuryHtml)

        val interceptor = MockInterceptor().apply {
            behavior(Behavior.UNORDERED)
            rule(get, url eq absoluteImageUrl) {
                respond(byteArrayOf(0x10, 0x20, 0x30))
                    .header("Content-Type", "image/jpeg")
            }
            rule(get, url eq resolvedRelativeImageUrl) {
                respond(byteArrayOf(0x40, 0x50))
                    .header("Content-Type", "image/png")
            }
        }
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val downloader = OfflineDownloader(
            database = database,
            mercuryParser = mercury,
            httpClient = httpClient,
            assetStore = OfflineAssetStore(rootDir = tempFolder.newFolder("offline")),
            perAssetMaxBytes = 1024 * 1024,
            retryAfterSeconds = 60,
        )

        val processed = downloader.run(
            maxArticles = 10,
            cacheLimitBytes = 0,
            cacheBufferBytes = 0,
        )

        assertEquals(1, processed)

        val reloaded = database.articlesQueries
            .findBy(articleID = article.id, mapper = ::articleMapper)
            .executeAsOne()
        val offlineHtml = reloaded.offlineHTML
        assertNotNull(offlineHtml, "offline_html should be populated")

        // The relative URL should be rewritten to absolute in stored HTML.
        assertContains(offlineHtml, resolvedRelativeImageUrl)
        assertContains(offlineHtml, absoluteImageUrl)
        assertFalse(
            offlineHtml.contains("src=\"$relativeImagePath\""),
            "relative src should be absolutized: $offlineHtml"
        )

        // Both assets should be persisted with their reported MIME types.
        val assets = database.offline_assetsQueries.findByArticle(article.id).executeAsList()
        assertEquals(2, assets.size)
        val byUrl = assets.associateBy { it.remote_url }
        assertEquals("image/jpeg", byUrl.getValue(absoluteImageUrl).mime_type)
        assertEquals("image/png", byUrl.getValue(resolvedRelativeImageUrl).mime_type)
    }

    @Test
    fun run_collapsesSrcsetIntoSingleSrc() = runTest {
        val feed = feedFixture.create(feedURL = "https://example.com/feed")
        runBlocking {
            FeedRecords(database).updateCacheOffline(feed.id, enabled = true)
        }
        val article = articleFixture.create(feed = feed, url = articleUrl)

        // Mimics The Verge's malformed output: src has a trailing srcset descriptor,
        // srcset enumerates many variants. The browser would pick a srcset URL.
        val small = "https://cdn.example.com/hero.jpg?w=376"
        val mercuryHtml = """
            <p>Article body text long enough to pass the parseable-body heuristic.</p>
            <img srcset="$small 376w, https://cdn.example.com/hero.jpg?w=768 768w, https://cdn.example.com/hero.jpg?w=1920 1920w"
                 src="$small 376w">
        """.trimIndent()

        val mercury = mockk<MercuryParser>()
        coEvery { mercury.fetch(any()) } returns Result.success(mercuryHtml)

        val interceptor = MockInterceptor().apply {
            behavior(Behavior.UNORDERED)
            rule(get, url eq small) {
                respond(byteArrayOf(0x01)).header("Content-Type", "image/jpeg")
            }
        }
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val downloader = OfflineDownloader(
            database = database,
            mercuryParser = mercury,
            httpClient = httpClient,
            assetStore = OfflineAssetStore(rootDir = tempFolder.newFolder("offline")),
            perAssetMaxBytes = 1024 * 1024,
            retryAfterSeconds = 60,
        )

        downloader.run(maxArticles = 10, cacheLimitBytes = 0, cacheBufferBytes = 0)

        val reloaded = database.articlesQueries
            .findBy(articleID = article.id, mapper = ::articleMapper)
            .executeAsOne()
        val offlineHtml = reloaded.offlineHTML.orEmpty()

        assertFalse(offlineHtml.contains("srcset"), "srcset should be stripped: $offlineHtml")
        assertFalse(offlineHtml.contains("376w"), "width descriptor should be gone: $offlineHtml")
        assertContains(offlineHtml, "src=\"$small\"")

        val assets = database.offline_assetsQueries.findByArticle(article.id).executeAsList()
        assertEquals(1, assets.size)
        assertEquals(small, assets.single().remote_url)
    }

    @Test
    fun run_fallsBackToFeedContent_whenMercuryReturnsEmptyWrapper() = runTest {
        val feed = feedFixture.create(feedURL = "https://example.com/feed")
        runBlocking {
            FeedRecords(database).updateCacheOffline(feed.id, enabled = true)
        }
        val feedBody = "<p>Real feed body text long enough to be parseable content.</p>"
        val article = runBlocking {
            val a = articleFixture.create(feed = feed, url = articleUrl)
            // Overwrite content_html with the feed body the renderer would have used.
            database.articlesQueries.create(
                id = a.id,
                feed_id = feed.id,
                title = a.title,
                author = a.author,
                content_html = feedBody,
                extracted_content_url = null,
                image_url = null,
                published_at = a.publishedAt.toEpochSecond(),
                summary = a.summary,
                url = a.url?.toString(),
                enclosure_type = null,
            )
            a
        }

        // Mercury returns just an empty SPA wrapper.
        val mercury = mockk<MercuryParser>()
        coEvery { mercury.fetch(any()) } returns Result.success("<div class=\"app-body\"></div>")

        val downloader = OfflineDownloader(
            database = database,
            mercuryParser = mercury,
            httpClient = OkHttpClient(),
            assetStore = OfflineAssetStore(rootDir = tempFolder.newFolder("offline")),
            perAssetMaxBytes = 1024,
            retryAfterSeconds = 60,
        )

        downloader.run(maxArticles = 10, cacheLimitBytes = 0, cacheBufferBytes = 0)

        val reloaded = database.articlesQueries
            .findBy(articleID = article.id, mapper = ::articleMapper)
            .executeAsOne()
        val offlineHtml = reloaded.offlineHTML.orEmpty()

        assertContains(offlineHtml, "Real feed body text")
        assertFalse(offlineHtml.contains("app-body"), "wrapper should not be stored: $offlineHtml")
    }

    @Test
    fun run_marksAttempted_whenMercuryFails() = runTest {
        val feed = feedFixture.create(feedURL = "https://example.com/feed")
        runBlocking {
            FeedRecords(database).updateCacheOffline(feed.id, enabled = true)
        }
        val article = articleFixture.create(feed = feed, url = articleUrl)

        val mercury = mockk<MercuryParser>()
        coEvery { mercury.fetch(any()) } returns Result.failure(RuntimeException("nope"))

        val downloader = OfflineDownloader(
            database = database,
            mercuryParser = mercury,
            httpClient = OkHttpClient(),
            assetStore = OfflineAssetStore(rootDir = tempFolder.newFolder("offline")),
            perAssetMaxBytes = 1024,
            retryAfterSeconds = 60,
        )

        val processed = downloader.run(
            maxArticles = 10,
            cacheLimitBytes = 0,
            cacheBufferBytes = 0,
        )

        assertEquals(0, processed)

        // Subsequent run within the retry window should skip this article entirely.
        val now = TimeHelpers.nowUTC().toEpochSecond()
        val nextCandidates = database.articlesQueries
            .findOfflineCandidates(retryCutoff = now - 60, limit = 10)
            .executeAsList()
        assertTrue(
            nextCandidates.none { it.id == article.id },
            "failed article should be excluded by retry cutoff"
        )
    }
}
