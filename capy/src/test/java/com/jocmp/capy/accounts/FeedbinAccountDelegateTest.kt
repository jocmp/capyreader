package com.jocmp.capy.accounts

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.feedbinclient.CreateSubscriptionRequest
import com.jocmp.feedbinclient.Entry
import com.jocmp.feedbinclient.Entry.Images
import com.jocmp.feedbinclient.Entry.Images.SizeOne
import com.jocmp.feedbinclient.Feedbin
import com.jocmp.feedbinclient.StarredEntriesRequest
import com.jocmp.feedbinclient.Subscription
import com.jocmp.feedbinclient.Tagging
import com.jocmp.feedbinclient.UnreadEntriesRequest
import com.jocmp.feedbinclient.UpdateSubscriptionRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedbinAccountDelegateTest {
    private val accountID = "777"
    private lateinit var database: Database
    private lateinit var feedbin: Feedbin
    private lateinit var feedFixture: FeedFixture

    private val subscriptions = listOf(
        Subscription(
            id = 1,
            created_at = "2024-01-30T19:42:44.851265Z",
            feed_id = 1,
            title = "Ed Zitron",
            feed_url = "http://wheresyoured.at/feed",
            site_url = "http://wheresyoured.at"
        ),
        Subscription(
            id = 2,
            created_at = "2022-04-27T22:06:16.639772Z",
            feed_id = 2,
            title = "Ars Technica",
            feed_url = "https://feeds.arstechnica.com/arstechnica/index",
            site_url = "http://wheresyoured.at"
        ),
    )

    private val taggings = listOf(
        Tagging(
            id = 1,
            feed_id = 2,
            name = "Gadgets"
        )
    )

    private val entries = listOf(
        Entry(
            id = 4375836222,
            feed_id = 2,
            title = "Reddit admits more moderator protests could hurt its business",
            summary = "Enlarge (credit: Jakub Porzycki/NurPhoto via Getty Images) Reddit filed to go public on Thursday (PDF), revealing various details of the social media company's inner workings. Among the revelations, Reddit acknowledged the threat of future user protests",
            content = "<p>Reddit filed to go public on Thursday (PDF), revealing various details of the social media company's inner workings. Among the revelations, Reddit acknowledged the threat of future user protests</p>",
            url = "https://arstechnica.com/?p=2005526",
            published = "2024-02-23T17:42:38.000000Z",
            created_at = "2024-02-23T17:47:45.708056Z",
            extracted_content_url = "https://extract.feedbin.com/parser/feedbin/fa2d8d34c403421a766dbec46c58738c36ff359e?base64_url=aHR0cHM6Ly9hcnN0ZWNobmljYS5jb20vP3A9MjAwNTUyNg==",
            author = "Scharon Harding",
            images = Images(
                size_1 = SizeOne(
                    cdn_url = "https://cdn.arstechnica.net/wp-content/uploads/2024/02/GettyImages-2023785321-800x534.jpg"
                ),
            ),
        )
    )

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        feedbin = mockk<Feedbin>()

        coEvery { feedbin.icons() }.returns(Response.success(listOf()))
    }

    @Test
    fun refreshAll_updatesEntries() = runTest {
        coEvery { feedbin.subscriptions() }.returns(Response.success(subscriptions))
        coEvery { feedbin.unreadEntries() }.returns(Response.success(entries.map { it.id }))
        coEvery { feedbin.starredEntries() }.returns(Response.success(emptyList()))
        coEvery { feedbin.taggings() }.returns(Response.success(taggings))
        coEvery {
            feedbin.entries(
                since = any(),
                perPage = any(),
                page = any(),
                ids = any(),
            )
        }.returns(Response.success(entries))

        val delegate = FeedbinAccountDelegate(database, feedbin)

        delegate.refresh()

        val articles = database
            .articlesQueries
            .countAll(read = false, starred = false)
            .executeAsList()

        val taggedNames = database
            .feedsQueries
            .tagged()
            .executeAsList()
            .map { it.name }

        val feeds = database
            .feedsQueries
            .all()
            .executeAsList()

        assertEquals(expected = 2, actual = feeds.size)

        assertEquals(expected = listOf(null, "Gadgets"), actual = taggedNames)

        assertEquals(expected = 1, actual = articles.size)
    }

    @Test
    fun refreshAll_findsMissingArticles() = runTest {
        coEvery { feedbin.subscriptions() }.returns(Response.success(subscriptions))
        coEvery { feedbin.unreadEntries() }.returns(Response.success(emptyList()))
        coEvery { feedbin.starredEntries() }.returns(Response.success(entries.map { it.id }))
        coEvery { feedbin.taggings() }.returns(Response.success(taggings))
        coEvery {
            feedbin.entries(
                since = null,
                perPage = 100,
                page = "1",
                ids = entries.map { it.id }.joinToString(","),
            )
        }.returns(Response.success(entries))
        coEvery {
            feedbin.entries(
                since = any(),
                perPage = 100,
                page = "1",
                ids = null,
            )
        }.returns(Response.success(emptyList()))

        val delegate = FeedbinAccountDelegate(database, feedbin)

        delegate.refresh()

        val articles = database
            .articlesQueries
            .countAll(read = false, starred = true)
            .executeAsList()

        assertEquals(expected = 1, actual = articles.size)
    }

    @Test
    fun markRead() = runTest {
        val id = 777L

        coEvery { feedbin.deleteUnreadEntries(body = any<UnreadEntriesRequest>()) } returns Response.success(
            null
        )

        val delegate = FeedbinAccountDelegate(database, feedbin)

        delegate.markRead(listOf(id.toString()))

        coVerify { feedbin.deleteUnreadEntries(body = UnreadEntriesRequest(listOf(id))) }
    }

    @Test
    fun markUnread() = runTest {
        val id = 777L

        coEvery { feedbin.createUnreadEntries(body = any<UnreadEntriesRequest>()) } returns Response.success(
            listOf(id)
        )

        val delegate = FeedbinAccountDelegate(database, feedbin)

        delegate.markUnread(listOf(id.toString()))

        coVerify { feedbin.createUnreadEntries(body = UnreadEntriesRequest(listOf(id))) }
    }

    @Test
    fun addStar() = runTest {
        val id = 777L

        coEvery { feedbin.createStarredEntries(body = any<StarredEntriesRequest>()) } returns Response.success(
            listOf(id)
        )

        val delegate = FeedbinAccountDelegate(database, feedbin)

        delegate.addStar(listOf(id.toString()))

        coVerify { feedbin.createStarredEntries(body = StarredEntriesRequest(listOf(id))) }
    }

    @Test
    fun removeStar() = runTest {
        val id = 777L

        coEvery { feedbin.deleteStarredEntries(body = any<StarredEntriesRequest>()) } returns Response.success(
            null
        )

        val delegate = FeedbinAccountDelegate(database, feedbin)

        delegate.removeStar(listOf(id.toString()))

        coVerify { feedbin.deleteStarredEntries(body = StarredEntriesRequest(listOf(id))) }
    }

    @Test
    fun addFeed() = runTest {
        val delegate = FeedbinAccountDelegate(database, feedbin)
        val url = "wheresyoured.at"
        val successResponse = Response.success<Subscription>(
            Subscription(
                id = 1330,
                created_at = "2024-01-30T19:42:44.851265Z",
                feed_id = 2819820,
                title = "Ed Zitron",
                feed_url = "http://wheresyoured.at",
                site_url = "http://wheresyoured.at",
            )
        )

        coEvery {
            feedbin.createSubscription(body = CreateSubscriptionRequest(feed_url = url))
        } returns successResponse

        val result = delegate.addFeed(url = url) as AddFeedResult.Success
        val feed = result.feed

        assertEquals(
            expected = "Ed Zitron",
            actual = feed.title
        )
    }

    @Test
    fun addFeed_multipleChoice() = runTest {
        val delegate = FeedbinAccountDelegate(database, feedbin)
        val url = "9to5google.com"
        val choices = listOf(
            SubscriptionChoice(
                feed_url = "9to5google.com/feed",
                title = "9to5Google"
            ),
            SubscriptionChoice(
                feed_url = "9to5google.com/comments/feed",
                title = "Comments for 9to5Google"
            ),
            SubscriptionChoice(
                feed_url = "9to5google.com/web-stories/feed",
                title = "Stories Archive - 9to5Google"
            )
        )

        val choicesBody = Json
            .encodeToJsonElement(choices)
            .toString()
            .toResponseBody("application/json".toMediaType())

        val multipleChoiceResponse = Response.error<Subscription>(
            choicesBody,
            okhttp3.Response.Builder().body(choicesBody)
                .code(300)
                .message("Response.error()")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build()
        )

        coEvery {
            feedbin.createSubscription(body = CreateSubscriptionRequest(feed_url = url))
        } returns multipleChoiceResponse

        val result = delegate.addFeed(url = url)

        val actualTitles =
            (result as AddFeedResult.MultipleChoices).choices.map { it.title }

        assertEquals(expected = choices.map { it.title }, actual = actualTitles)
    }

    @Test
    fun addFeed_Failure() = runTest {
        val delegate = FeedbinAccountDelegate(database, feedbin)
        val url = "example.com"

        val responseBody = """
            {
              "status": 404,
              "message": null,
              "errors": []
            }
        """.toResponseBody(contentType = "application/json".toMediaType())

        coEvery {
            feedbin.createSubscription(body = CreateSubscriptionRequest(feed_url = url))
        } returns Response.error(404, responseBody)

        val result = delegate.addFeed(url = url)

        assertTrue(result is AddFeedResult.Failure)
    }

    @Test
    fun updateFeed_modifyTitle() = runTest {
        val delegate = FeedbinAccountDelegate(database, feedbin)
        val feed = feedFixture.create()

        val subscription = Subscription(
            id = feed.subscriptionID.toLong(),
            created_at = "2024-01-30T19:42:44.851265Z",
            feed_id = feed.id.toLong(),
            title = feed.title,
            feed_url = feed.feedURL,
            site_url = feed.siteURL
        )

        val feedTitle = "The Verge Mobile Podcast"

        coEvery {
            feedbin.updateSubscription(
                subscriptionID = feed.subscriptionID,
                body = UpdateSubscriptionRequest(title = feedTitle)
            )
        }.returns(Response.success(subscription))

        val updated = delegate.updateFeed(
            feed = feed,
            title = feedTitle,
            folderTitles = emptyList()
        ).getOrThrow()

        assertEquals(expected = feedTitle, actual = updated.title)
    }
}
