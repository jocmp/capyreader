package com.jocmp.capy.accounts.feedbin

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.InMemoryDataStore
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.SubscriptionChoice
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.EnclosureRecords
import com.jocmp.capy.randomID
import com.jocmp.feedbinclient.CreateSubscriptionRequest
import com.jocmp.feedbinclient.Enclosure
import com.jocmp.feedbinclient.Entry
import com.jocmp.feedbinclient.Feedbin
import com.jocmp.feedbinclient.SavedSearch
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
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedbinAccountDelegateTest {
    private val accountID = "777"
    private lateinit var database: Database
    private lateinit var feedbin: Feedbin
    private lateinit var feedFixture: FeedFixture
    private lateinit var delegate: AccountDelegate

    private val subscriptions = listOf(
        Subscription(
            id = 1,
            created_at = "2022-04-27T22:06:16.639772Z",
            feed_id = 2,
            title = "Ars Technica",
            feed_url = "https://feeds.arstechnica.com/arstechnica/index",
            site_url = "http://wheresyoured.at"
        ),
        Subscription(
            id = 2,
            created_at = "2024-01-30T19:42:44.851265Z",
            feed_id = 5,
            title = "The Verge",
            feed_url = "https://www.theverge.com/rss/index.xml",
            site_url = "http://theverge.com"
        ),
    )

    private val taggings = listOf(
        Tagging(
            id = 1,
            feed_id = 2,
            name = "Gadgets"
        )
    )

    private val arsTechnicaArticle = Entry(
        id = 4375836222,
        feed_id = 2,
        title = "Reddit admits more moderator protests could hurt its business",
        summary = "Enlarge (credit: Jakub Porzycki/NurPhoto via Getty Images) Reddit filed to go public on Thursday (PDF), revealing various details of the social media company's inner workings. Among the revelations, Reddit acknowledged the threat of future user protests",
        content = "<p>Reddit filed to go public on Thursday (PDF), revealing various details of the social media company's inner workings. Among the revelations, Reddit acknowledged the threat of future user protests</p>",
        url = "https://arstechnica.com/?p=2005526",
        published = "2024-02-23T17:42:38.000000Z",
        created_at = "2024-02-23T17:47:45.708056Z",
        extracted_content_url = "https://extract.feedbin.com/parser/feedbin/...",
        author = "Scharon Harding",
        images = Entry.Images(
            original_url = "https://cdn.arstechnica.net/wp-content/uploads/2024/02/GettyImages-2023785321-800x534.jpg",
            size_1 = Entry.Images.SizeOne(
                cdn_url = "https://cdn.arstechnica.net/wp-content/uploads/2024/02/GettyImages-2023785321-800x534.jpg"
            ),
        ),
    )

    private val entries = listOf(arsTechnicaArticle)

    private val vergeArticle = Entry(
        id = 4718104685,
        feed_id = 5,
        title = "Amazfit Active 2 review: outsized bang for your buck",
        summary = "This $130 smartwatch certainly doesn't look it. | Photo by Amelia Holowaty Krales / The Verge A common reader request I get goes something like this: what should I buy if I don't want a smartwatch but want basic fitness tracking? When I suggest",
        content = "<figure>\n\n<img alt=\"Close-up view of the Amazfit Active 2's screen,...",
        url = "https://www.theverge.com/smartwatch-review/608342/amazfit-active-2-review-budget-smartwatch-wearables-fitness-tracker",
        published = "2025-02-09T14:00:00.000000Z",
        created_at = "2025-02-09T14:02:28.994289Z",
        extracted_content_url = "https://extract.feedbin.com/...",
        author = "Victoria Song",
        images = null,
        enclosure = Enclosure(
            enclosure_url = "https://www.podtrac.com/pts/redirect.mp3/pdst.fm/e/chtbl.com/track/524GE/traffic.megaphone.fm/VMP2413819050.mp3?updated=1725568071",
            enclosure_type = "audio/mpeg",
            enclosure_length = "0",
            itunes_duration = "3000",
            itunes_image = "https://megaphone.imgix.net/podcasts/61bb0bc2-3d4b-11ef-b6cd-7b25b6cb2486/image/4f33aa95b74cece19e7d86d0ce61328f.png?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress"
        )
    )

    private val savedSearch = SavedSearch(
        id = randomID(),
        name = "Pebble",
        query = "Pebble",
    )

    @BeforeTest
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        feedbin = mockk()
        delegate = FeedbinAccountDelegate(database, feedbin, AccountPreferences(InMemoryDataStore()))

        coEvery { feedbin.icons() }.returns(Response.success(listOf()))
    }

    @Test
    fun refresh_updatesEntries() = runTest {
        coEvery { feedbin.subscriptions() }.returns(Response.success(subscriptions))
        coEvery { feedbin.unreadEntries() }.returns(
            Response.success(
                listOf(
                    arsTechnicaArticle.id,
                    vergeArticle.id
                )
            )
        )
        coEvery { feedbin.starredEntries() }.returns(Response.success(emptyList()))
        coEvery { feedbin.taggings() }.returns(Response.success(taggings))
        coEvery { feedbin.savedSearches() }.returns(Response.success(listOf(savedSearch)))
        coEvery { feedbin.savedSearchEntries(any()) }.returns(Response.success(listOf(vergeArticle.id)))
        coEvery {
            feedbin.entries(
                since = any(),
                perPage = any(),
                page = any(),
                ids = null,
            )
        }.returns(Response.success(entries))

        coEvery {
            feedbin.entries(
                since = any(),
                perPage = any(),
                page = any(),
                ids = vergeArticle.id.toString(),
            )
        }.returns(Response.success(listOf(vergeArticle)))

        delegate.refresh(ArticleFilter.default())

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

        assertEquals(expected = 2, actual = articles.size)

        val enclosures = EnclosureRecords(database).byArticle(vergeArticle.id.toString())
        assertEquals(expected = 1, actual = enclosures.size)
    }

    @Test
    fun refresh_IOException() = runTest {
        val networkError = SocketTimeoutException("Sorry networked charlie")
        coEvery { feedbin.subscriptions() }.throws(networkError)

        val result = delegate.refresh(ArticleFilter.default())

        assertEquals(result, Result.failure(networkError))
    }

    @Test
    fun refresh_findsMissingArticles() = runTest {
        val unreadEntry = Entry(
            id = 1,
            feed_id = 2,
            title = "Reddit admits more moderator protests could hurt its business",
            summary = "Enlarge (credit: Jakub Porzycki/NurPhoto via Getty Images) Reddit filed to go public on Thursday (PDF), revealing various details of the social media company's inner workings. Among the revelations, Reddit acknowledged the threat of future user protests",
            content = "<p>Reddit filed to go public on Thursday (PDF), revealing various details of the social media company's inner workings. Among the revelations, Reddit acknowledged the threat of future user protests</p>",
            url = "https://arstechnica.com/?p=2005526",
            published = "2024-02-23T17:42:38.000000Z",
            created_at = "2024-02-23T17:47:45.708056Z",
            extracted_content_url = "https://extract.feedbin.com/parser/feedbin/fa2d8d34c403421a766dbec46c58738c36ff359e?base64_url=aHR0cHM6Ly9hcnN0ZWNobmljYS5jb20vP3A9MjAwNTUyNg==",
            author = "Scharon Harding",
            images = Entry.Images(
                original_url = "https://cdn.arstechnica.net/wp-content/uploads/2024/02/GettyImages-2023785321-800x534.jpg",
                size_1 = Entry.Images.SizeOne(
                    cdn_url = "https://cdn.arstechnica.net/wp-content/uploads/2024/02/GettyImages-2023785321-800x534.jpg"
                ),
            ),
        )

        val readEntry = Entry(
            id = 2,
            feed_id = 2,
            title = "Apple’s iPhone 16 launch event is set for September",
            summary = "Apple’s tagline: 'It’s Glowtime.'",
            content = "<p>More content here</p>",
            url = "https://www.theverge.com/2024/8/26/24223957/apple-iphone-16-launch-event-date-glowtime",
            published = "2024-08-24T17:42:38.000000Z",
            created_at = "2024-08-243T17:47:45.708056Z",
            extracted_content_url = "https://extract.feedbin.com/parser/feedbin/fa2d8d34c403421a766dbec46c58738c36ff359e?base64_url=aHR0cHM6Ly9hcnN0ZWNobmljYS5jb20vP3A9MjAwNTUyNg==",
            author = "Jay Peters",
            images = Entry.Images(
                original_url = "https://cdn.arstechnica.net/wp-content/uploads/2024/02/GettyImages-2023785321-800x534.jpg",
                size_1 = Entry.Images.SizeOne(
                    cdn_url = "https://cdn.arstechnica.net/wp-content/uploads/2024/02/GettyImages-2023785321-800x534.jpg"
                ),
            ),
        )

        val starredEntries = listOf(unreadEntry, readEntry)

        coEvery { feedbin.subscriptions() }.returns(Response.success(subscriptions))
        coEvery { feedbin.unreadEntries() }.returns(Response.success(listOf(unreadEntry.id)))
        coEvery { feedbin.starredEntries() }.returns(Response.success(starredEntries.map { it.id }))
        coEvery { feedbin.taggings() }.returns(Response.success(taggings))
        coEvery { feedbin.savedSearches() }.returns(Response.success(emptyList()))
        coEvery {
            feedbin.entries(
                since = null,
                perPage = 100,
                page = "1",
                ids = starredEntries.map { it.id }.joinToString(","),
            )
        }.returns(Response.success(starredEntries))
        coEvery {
            feedbin.entries(
                since = any(),
                perPage = 100,
                page = "1",
                ids = null,
            )
        }.returns(Response.success(emptyList()))

        delegate.refresh(ArticleFilter.default())

        val starredArticles = ArticleRecords(database)
            .byStatus
            .all(
                ArticleStatus.STARRED,
                limit = 2,
                offset = 0,
                sortOrder = SortOrder.NEWEST_FIRST,
            )
            .executeAsList()

        val unreadArticle = starredArticles.find { it.id == unreadEntry.id.toString() }!!
        val readArticle = starredArticles.find { it.id == readEntry.id.toString() }!!

        assertFalse(unreadArticle.read)
        assertTrue(readArticle.read)
    }

    @Test
    fun markRead() = runTest {
        val id = 777L

        coEvery { feedbin.deleteUnreadEntries(body = any<UnreadEntriesRequest>()) } returns Response.success(
            null
        )

        delegate.markRead(listOf(id.toString()))

        coVerify { feedbin.deleteUnreadEntries(body = UnreadEntriesRequest(listOf(id))) }
    }

    @Test
    fun markUnread() = runTest {
        val id = 777L

        coEvery { feedbin.createUnreadEntries(body = any<UnreadEntriesRequest>()) } returns Response.success(
            listOf(id)
        )

        delegate.markUnread(listOf(id.toString()))

        coVerify { feedbin.createUnreadEntries(body = UnreadEntriesRequest(listOf(id))) }
    }

    @Test
    fun addStar() = runTest {
        val id = 777L

        coEvery { feedbin.createStarredEntries(body = any<StarredEntriesRequest>()) } returns Response.success(
            listOf(id)
        )

        delegate.addStar(listOf(id.toString()))

        coVerify { feedbin.createStarredEntries(body = StarredEntriesRequest(listOf(id))) }
    }

    @Test
    fun removeStar() = runTest {
        val id = 777L

        coEvery { feedbin.deleteStarredEntries(body = any<StarredEntriesRequest>()) } returns Response.success(
            null
        )

        delegate.removeStar(listOf(id.toString()))

        coVerify { feedbin.deleteStarredEntries(body = StarredEntriesRequest(listOf(id))) }
    }

    @Test
    fun addFeed() = runTest {
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

        coEvery { feedbin.unreadEntries() }.returns(Response.success(entries.map { it.id }))
        coEvery { feedbin.starredEntries() }.returns(Response.success(emptyList()))
        coEvery {
            feedbin.entries(
                since = any(),
                perPage = any(),
                page = any(),
                ids = any(),
            )
        }.returns(Response.success(emptyList()))

        val result = delegate.addFeed(
            url = url,
            folderTitles = emptyList(),
            title = ""
        ) as AddFeedResult.Success
        val feed = result.feed

        assertEquals(
            expected = "Ed Zitron",
            actual = feed.title
        )
    }

    @Test
    fun addFeed_multipleChoice() = runTest {
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

        val result = delegate.addFeed(url = url, folderTitles = emptyList(), title = "")

        val actualTitles =
            (result as AddFeedResult.MultipleChoices).choices.map { it.title }

        assertEquals(expected = choices.map { it.title }, actual = actualTitles)
    }

    @Test
    fun addFeed_Failure() = runTest {
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

        val result = delegate.addFeed(url = url, folderTitles = emptyList(), title = "")

        assertTrue(result is AddFeedResult.Failure)
    }

    @Test
    fun updateFeed_modifyTitle() = runTest {
        val delegate = FeedbinAccountDelegate(database, feedbin, AccountPreferences(InMemoryDataStore()))
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
            folderTitles = emptyList(),
        ).getOrThrow()

        assertEquals(expected = feedTitle, actual = updated.title)
    }
}
