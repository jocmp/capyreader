package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.readerclient.Category
import com.jocmp.readerclient.GoogleReader
import com.jocmp.readerclient.Item
import com.jocmp.readerclient.Item.Link
import com.jocmp.readerclient.Item.Origin
import com.jocmp.readerclient.Item.Summary
import com.jocmp.readerclient.ItemRef
import com.jocmp.readerclient.Stream
import com.jocmp.readerclient.StreamItemIDsResult
import com.jocmp.readerclient.StreamItemsContentsResult
import com.jocmp.readerclient.Subscription
import com.jocmp.readerclient.SubscriptionListResult
import com.jocmp.readerclient.SubscriptionQuickAddResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.net.SocketTimeoutException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReaderAccountDelegateTest {
    private val accountID = "777"
    private val postToken = "alice/foobar123"
    private lateinit var database: Database
    private lateinit var googleReader: GoogleReader
    private lateinit var feedFixture: FeedFixture
    private lateinit var delegate: AccountDelegate

    private val arsTechnica = Subscription(
        id = "feed/2",
        title = "Ars Technica - All content",
        categories = listOf(
            Category(
                id = "user/1/label/Gadgets",
                label = "Gadgets"
            )
        ),
        url = "https://feeds.arstechnica.com/arstechnica/index",
        htmlUrl = "https://arstechnica.com",
        iconUrl = "",
    )

    private val subscriptions = listOf(
        arsTechnica,
        Subscription(
            id = "feed/3",
            title = "The Verge",
            categories = listOf(
                Category(
                    id = "user/1/label/All",
                    label = "All"
                )
            ),
            url = "https://www.theverge.com/rss/index.xml",
            htmlUrl = "https://theverge.com",
            iconUrl = "",
        ),
    )

    private val items = listOf(
        Item(
            id = "tag:google.com,2005:reader/item/0000000000000010",
            published = 1723806013,
            title = "Rocket Report: ULA is losing engineers; SpaceX is launching every two days",
            canonical = listOf(Link("https://arstechnica.com/?p=2043638")),
            origin = Origin(
                streamId = "feed/2",
                title = "Ars Technica - All content",
                htmlUrl = "https://arstechnica.com",
            ),
            summary = Summary("Summary - Welcome to Edition 7.07 of the Rocket Report! SpaceX has not missed a beat since the Federal Aviation Administration gave the company a green light to resume Falcon 9 launches after a failure last month."),
            content = Item.Content("Content - Welcome to Edition 7.07 of the Rocket Report! SpaceX has not missed a beat since the Federal Aviation Administration gave the company a green light to resume Falcon 9 launches after a failure last month."),
        )
    )

    @BeforeTest
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        googleReader = mockk()

        delegate = ReaderAccountDelegate(database, googleReader)
    }

    @Test
    fun refresh_updatesEntries() = runTest {
        val itemRefs = listOf(ItemRef("16"))

        stubSubscriptions()
        stubUnread(itemRefs)
        stubStarred()
        stubReadingList(itemRefs)

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

        assertEquals(expected = listOf("All", "Gadgets"), actual = taggedNames)

        assertEquals(expected = 1, actual = articles.size)
    }

    @Test
    fun refresh_findsMissingArticles() = runTest {
        val itemRefs = listOf("1", "16").map { ItemRef(it) }

        stubSubscriptions()
        stubUnread(itemRefs)
        stubStarred(listOf("1", "2").map { ItemRef(it) })

        val unreadItem = Item(
            id = "tag:google.com,2005:reader/item/0000000000000001",
            published = 1708710158,
            title = "Reddit admits more moderator protests could hurt its business",
            canonical = listOf(Link("https://arstechnica.com/?p=2005526")),
            author = "Scharon Harding",
            origin = Origin(
                streamId = "feed/2",
                title = "Ars Technica - All content",
                htmlUrl = "https://arstechnica.com",
            ),
            summary = Summary("Enlarge (credit: Jakub Porzycki/NurPhoto via Getty Images) Reddit filed to go public on Thursday (PDF), revealing various details of the social media company's inner workings. Among the revelations, Reddit acknowledged the threat of future user protests"),
        )

        val readItem = Item(
            id = "tag:google.com,2005:reader/item/0000000000000002",
            title = "Apple’s iPhone 16 launch event is set for September",
            summary = Summary("Apple’s tagline: 'It’s Glowtime.'"),
            canonical = listOf(Link("https://www.theverge.com/2024/8/26/24223957/apple-iphone-16-launch-event-date-glowtime")),
            published = 1724521358,
            author = "Jay Peters",
            origin = Origin(
                streamId = "feed/3",
                title = "The Verge",
                htmlUrl = "https://theverge.com",
            ),
        )

        stubReadingList(itemRefs, listOf(unreadItem, items.first()))

        val starredItems = listOf(unreadItem, readItem)

        coEvery {
            googleReader.streamItemsContents(starredItems.map { it.hexID }, postToken = postToken)
        }.returns(Response.success(StreamItemsContentsResult(starredItems)))

        delegate.refresh()

        val starredArticles = ArticleRecords(database)
            .byStatus
            .all(
                ArticleStatus.STARRED,
                limit = 2,
                offset = 0,
                unreadSort = UnreadSortOrder.NEWEST_FIRST,
            )
            .executeAsList()

        val unreadArticle = starredArticles.find { it.id == unreadItem.hexID }!!
        val readArticle = starredArticles.find { it.id == readItem.hexID }!!

        assertFalse(unreadArticle.read)
        assertTrue(readArticle.read)
    }

    @Test
    fun refresh_IOException() = runTest {
        val networkError = SocketTimeoutException("Sorry networked charlie")
        coEvery { googleReader.subscriptionList() }.throws(networkError)

        val result = delegate.refresh()

        assertEquals(result, Result.failure(networkError))
    }

    @Test
    fun markRead() = runTest {
        val id = "0006265cd4de43c6"

        coEvery {
            googleReader.editTag(
                ids = listOf(id),
                postToken = postToken,
                addTag = Stream.READ.id,
            )
        } returns Response.success("OK")

        stubPostToken()

        delegate.markRead(listOf(id))

        coVerify {
            googleReader.editTag(
                ids = listOf(id),
                postToken = postToken,
                addTag = Stream.READ.id,
            )
        }
    }

    @Test
    fun markUnread() = runTest {
        val id = "0006265cd4de43c6"

        coEvery {
            googleReader.editTag(
                ids = listOf(id),
                postToken = postToken,
                removeTag = Stream.READ.id,
            )
        } returns Response.success("OK")

        stubPostToken()

        delegate.markUnread(listOf(id))

        coVerify {
            googleReader.editTag(
                ids = listOf(id),
                postToken = postToken,
                removeTag = Stream.READ.id,
            )
        }
    }

    @Test
    fun addStar() = runTest {
        val id = "0006265cd4de43c6"

        coEvery {
            googleReader.editTag(
                ids = listOf(id),
                postToken = postToken,
                addTag = Stream.STARRED.id,
            )
        } returns Response.success("OK")

        stubPostToken()

        delegate.addStar(listOf(id))

        coVerify {
            googleReader.editTag(
                ids = listOf(id),
                postToken = postToken,
                addTag = Stream.STARRED.id,
            )
        }
    }

    @Test
    fun removeStar() = runTest {
        val id = "0006265cd4de43c6"

        coEvery {
            googleReader.editTag(
                ids = listOf(id),
                postToken = postToken,
                removeTag = Stream.STARRED.id,
            )
        } returns Response.success("OK")

        stubPostToken()

        delegate.removeStar(listOf(id))

        coVerify {
            googleReader.editTag(
                ids = listOf(id),
                postToken = postToken,
                removeTag = Stream.STARRED.id,
            )
        }
    }

    @Test
    fun addFeed() = runTest {
        stubPostToken()
        stubUnread()
        stubStarred()
        stubReadingList()

        val subscription = Subscription(
            id = "feed/4",
            title = "404 Media",
            categories = listOf(
                Category(
                    id = "user/1/label/All",
                    label = "All"
                )
            ),
            url = "https://www.404media.co/rss/",
            htmlUrl = "https://www.404media.co/",
            iconUrl = "",
        )

        stubSubscriptions(subscriptions + listOf(subscription))

        val response = Response.success(
            SubscriptionQuickAddResult(
                numResults = 1,
                streamId = "feed/4",
                streamName = "404 Media",
                query = "https://www.404media.co/rss/"
            )
        )

        val url = "https://404media.co"

        coEvery {
            googleReader.quickAddSubscription(url = url, postToken = postToken)
        } returns response

        val result = delegate.addFeed(
            url = url,
            folderTitles = emptyList(),
            title = ""
        ) as AddFeedResult.Success

        val feed = result.feed

        assertEquals(expected = subscription.id, actual = feed.id)
        assertEquals(expected = subscription.title, actual = feed.title)
        assertEquals(expected = subscription.url, actual = feed.feedURL)
        assertEquals(expected = subscription.htmlUrl, actual = feed.siteURL)
    }

    @Test
    fun addFeed_validationError() = runTest {
        stubPostToken()

        val response = Response.success(
            SubscriptionQuickAddResult(
                numResults = 0,
            )
        )

        coEvery {
            googleReader.quickAddSubscription(url = "https://theverge.com", postToken = postToken)
        } returns response

        val result = delegate.addFeed(
            url = "theverge.com",
            folderTitles = emptyList(),
            title = ""
        ) as AddFeedResult.Failure

        assertTrue(result.error is AddFeedResult.Error.FeedNotFound)
    }

    @Test
    fun addFeed_networkError() = runTest {
        stubPostToken()

        val responseBody = """
            {
              "error_message": "This feed already exists."
            }
        """.toResponseBody(contentType = "application/json".toMediaType())

        coEvery {
            googleReader.quickAddSubscription(url = "https://theverge.com", postToken = postToken)
        } returns Response.error(500, responseBody)

        val result = delegate.addFeed(
            url = "theverge.com",
            folderTitles = emptyList(),
            title = ""
        ) as AddFeedResult.Failure

        assertTrue(result.error is AddFeedResult.Error.FeedNotFound)
    }


    @Test
    fun updateFeed_modifyTitle() = runTest {
        stubPostToken()

        val feed = feedFixture.create(
            feedID = "feed/2",
            title = "something else"
        )

        val feedTitle = "Ars Technica"

        coEvery {
            googleReader.editSubscription(
                id = feed.id,
                actionID = "edit",
                title = feedTitle,
                addCategoryID = "user/-/label/Tech",
                postToken = postToken
            )
        }.returns(Response.success("OK"))

        val updated = delegate.updateFeed(
            feed = feed,
            title = feedTitle,
            folderTitles = listOf("Tech"),
        ).getOrThrow()

        assertEquals(expected = feedTitle, actual = updated.title)
    }

    @Test
    fun removeFeed() = runTest {
        stubPostToken()

        val feed = feedFixture.create(feedID = "feed/2")

        coEvery {
            googleReader.editSubscription(
                id = feed.id,
                actionID = "unsubscribe",
                postToken = postToken
            )
        }.returns(Response.success("OK"))

        val result = delegate.removeFeed(feed = feed)

        assertTrue(result.isSuccess)
    }

    @Test
    fun removeFeed_networkError() = runTest {
        stubPostToken()

        val feed = feedFixture.create(feedID = "feed/2")

        coEvery {
            googleReader.editSubscription(
                id = feed.id,
                actionID = "unsubscribe",
                postToken = postToken
            )
        }.returns(Response.error(400, "Bad Request!".toResponseBody()))

        val result = delegate.removeFeed(feed = feed)

        assertTrue(result.isFailure)
    }

    private fun stubSubscriptions(subscriptions: List<Subscription> = this.subscriptions) {
        coEvery { googleReader.subscriptionList() }.returns(
            Response.success(
                SubscriptionListResult(
                    subscriptions
                )
            )
        )
    }

    private fun stubStarred(itemRefs: List<ItemRef> = emptyList()) {
        coEvery {
            googleReader.streamItemsIDs(
                streamID = Stream.STARRED.id,
            )
        }.returns(
            Response.success(
                StreamItemIDsResult(
                    itemRefs = itemRefs,
                    continuation = null
                )
            )
        )
    }

    private fun stubReadingList(
        itemRefs: List<ItemRef> = emptyList(),
        items: List<Item> = this.items
    ) {
        coEvery {
            googleReader.streamItemsIDs(
                streamID = Stream.READING_LIST.id,
                since = any(),
                count = 100,
                excludedStreamID = Stream.READ.id
            )
        }.returns(Response.success(StreamItemIDsResult(itemRefs = itemRefs, continuation = null)))

        val errorResponse = okhttp3.Response.Builder()
            .code(401)
            .protocol(Protocol.HTTP_1_1)
            .headers(Headers.headersOf(GoogleReader.BAD_TOKEN_HEADER_KEY, "true"))
            .message("Unauthorized")
            .request(
                Request.Builder().url("http://localhost/").build()
            ).build()

        coEvery {
            googleReader.streamItemsContents(items.map { it.hexID }, postToken = null)
        }.returns(Response.error("".toResponseBody(), errorResponse))

        stubPostToken()

        coEvery {
            googleReader.streamItemsContents(items.map { it.hexID }, postToken = postToken)
        }.returns(Response.success(StreamItemsContentsResult(items)))
    }

    private fun stubPostToken() {
        coEvery {
            googleReader.token()
        }.returns(Response.success(postToken))
    }

    private fun stubUnread(itemRefs: List<ItemRef> = emptyList()) {
        coEvery {
            googleReader.streamItemsIDs(
                streamID = Stream.READING_LIST.id,
                count = 10_000,
                excludedStreamID = Stream.READ.id,
            )
        }.returns(Response.success(StreamItemIDsResult(itemRefs = itemRefs, continuation = null)))
    }
}
