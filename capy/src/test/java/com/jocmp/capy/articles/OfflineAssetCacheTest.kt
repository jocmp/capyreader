package com.jocmp.capy.articles

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.SyncStatus
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.persistence.SyncStatusRecords
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.jsoup.Jsoup
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineAssetCacheTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var database: Database
    private lateinit var feedFixture: FeedFixture
    private lateinit var articleFixture: ArticleFixture
    private lateinit var syncStatusRecords: SyncStatusRecords
    private lateinit var storageDir: File
    private lateinit var cache: OfflineAssetCache

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        feedFixture = FeedFixture(database)
        articleFixture = ArticleFixture(database)
        syncStatusRecords = SyncStatusRecords(database)
        storageDir = tempFolder.newFolder("offline_assets")

        cache = OfflineAssetCache(
            database = database,
            mercuryParser = MercuryParser(OkHttpClient(), "ua", "en"),
            httpClient = OkHttpClient(),
            storageDir = storageDir,
            syncStatusRecords = syncStatusRecords,
        )
    }

    @Test
    fun `computeDesired returns newest articles up to the limit`() {
        val feed = feedFixture.create()
        val start = nowUTC().minusMonths(1)
        val articles = (0 until 5).map { i ->
            articleFixture.create(
                feed = feed,
                publishedAt = start.plusHours(i.toLong()).toEpochSecond(),
            )
        }

        val desired = cache.computeDesired(limit = 3)

        // Newest three by published_at desc (indices 4, 3, 2)
        assertEquals(setOf(articles[4].id, articles[3].id, articles[2].id), desired.keys)
    }

    @Test
    fun `computeDesired is empty when limit is zero`() {
        val feed = feedFixture.create()
        articleFixture.create(feed = feed)

        val desired = cache.computeDesired(limit = 0)

        assertTrue(desired.isEmpty())
    }

    @Test
    fun `computeDesired excludes articles from feeds with offline_enabled = false`() {
        val opted = feedFixture.create(title = "Opted in")
        val skipped = feedFixture.create(title = "Skipped")
        database.feedsQueries.updateOfflineEnabled(enabled = false, feedID = skipped.id)

        val optedArticle = articleFixture.create(feed = opted)
        val skippedArticle = articleFixture.create(feed = skipped)

        val desired = cache.computeDesired(limit = 10)

        assertTrue(desired.containsKey(optedArticle.id))
        assertFalse(desired.containsKey(skippedArticle.id))
    }

    @Test
    fun `computeDesired ignores starred status (no special-casing)`() {
        val feed = feedFixture.create()
        val start = nowUTC().minusMonths(1)
        val newer = articleFixture.create(
            feed = feed,
            publishedAt = start.plusDays(5).toEpochSecond(),
        )
        val starredOlder = articleFixture.create(
            feed = feed,
            publishedAt = start.minusDays(5).toEpochSecond(),
            starred = true,
        )

        val desired = cache.computeDesired(limit = 1)

        // Newest wins regardless of star
        assertTrue(desired.containsKey(newer.id))
        assertFalse(desired.containsKey(starredOlder.id))
    }

    @Test
    fun `enqueueCandidates inserts CACHE rows for uncached desired articles`() {
        val feed = feedFixture.create()
        val a = articleFixture.create(feed = feed)
        val b = articleFixture.create(feed = feed)

        cache.enqueueCandidates(limit = 10)

        val pending = syncStatusRecords.pendingArticleIDs(SyncStatus.Key.CACHE).toSet()
        assertEquals(setOf(a.id, b.id), pending)
    }

    @Test
    fun `enqueueCandidates skips articles already cached`() {
        val feed = feedFixture.create()
        val a = articleFixture.create(feed = feed)
        val b = articleFixture.create(feed = feed)

        database.articlesQueries.setOfflineHtml(html = "<p>cached</p>", articleID = a.id)

        cache.enqueueCandidates(limit = 10)

        val pending = syncStatusRecords.pendingArticleIDs(SyncStatus.Key.CACHE).toSet()
        assertEquals(setOf(b.id), pending)
    }

    @Test
    fun `evict clears offline_html and deletes asset dirs for articles beyond the limit`() {
        val feed = feedFixture.create()
        val start = nowUTC().minusMonths(1)
        val articles = (0 until 4).map { i ->
            articleFixture.create(
                feed = feed,
                publishedAt = start.plusHours(i.toLong()).toEpochSecond(),
            )
        }

        articles.forEach { article ->
            database.articlesQueries.setOfflineHtml(
                html = "<p>cached ${article.id}</p>",
                articleID = article.id,
            )
            File(storageDir, article.id).apply {
                mkdirs()
                File(this, "asset.jpg").writeText("data")
            }
        }

        cache.evict(limit = 2)

        // Oldest two are evicted, newest two kept
        val evicted = listOf(articles[0].id, articles[1].id)
        val kept = listOf(articles[2].id, articles[3].id)

        evicted.forEach { id ->
            assertFalse(File(storageDir, id).exists(), "expected asset dir removed for $id")
        }
        kept.forEach { id ->
            assertTrue(File(storageDir, id).exists(), "expected asset dir preserved for $id")
        }

        val cachedIDs = database.articlesQueries.findCachedArticleIDs().executeAsList().toSet()
        assertEquals(kept.toSet(), cachedIDs)
    }

    @Test
    fun `bestSrcsetUrl picks the largest width descriptor`() {
        val best = cache.bestSrcsetUrl(
            "https://ex.com/small.jpg 300w, https://ex.com/large.jpg 800w, https://ex.com/medium.jpg 600w",
            baseUri = "https://ex.com",
        )
        assertEquals("https://ex.com/large.jpg", best)
    }

    @Test
    fun `bestSrcsetUrl picks the largest density descriptor`() {
        val best = cache.bestSrcsetUrl(
            "https://ex.com/1x.jpg 1x, https://ex.com/3x.jpg 3x, https://ex.com/2x.jpg 2x",
            baseUri = "https://ex.com",
        )
        assertEquals("https://ex.com/3x.jpg", best)
    }

    @Test
    fun `bestSrcsetUrl resolves relative URLs against base`() {
        val best = cache.bestSrcsetUrl(
            "/img/small.jpg 100w, /img/large.jpg 900w",
            baseUri = "https://ex.com/articles/post",
        )
        assertEquals("https://ex.com/img/large.jpg", best)
    }

    @Test
    fun `narrowSrcsets collapses srcset attribute to a single URL`() {
        val doc = Jsoup.parse(
            """
                <html><body>
                  <img srcset="https://ex.com/sm.jpg 200w, https://ex.com/lg.jpg 1200w">
                </body></html>
            """.trimIndent(),
            "https://ex.com",
        )

        cache.narrowSrcsets(doc)

        val srcset = doc.selectFirst("img")!!.attr("srcset")
        assertEquals("https://ex.com/lg.jpg", srcset)
    }
}
