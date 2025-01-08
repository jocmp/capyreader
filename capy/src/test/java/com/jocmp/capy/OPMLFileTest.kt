package com.jocmp.capy

import com.jocmp.capy.accounts.FakeFaviconFetcher
import com.jocmp.capy.accounts.LocalAccountDelegate
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.AccountFixture
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.fixtures.FolderFixture
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class OPMLFileTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    private val accountID = "777"
    private val httpClient = mockk<OkHttpClient>()
    private lateinit var account: Account
    private lateinit var database: Database
    private lateinit var feedFixture: FeedFixture
    private lateinit var tagFixture: FolderFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        tagFixture = FolderFixture(database)

        val delegate = LocalAccountDelegate(
            database = database,
            httpClient = httpClient,
            feedFinder = MockFeedFinder(),
            faviconFetcher = FakeFaviconFetcher
        )

        account = AccountFixture.create(
            id = accountID,
            database = database,
            parentFolder = folder,
            accountDelegate = delegate
        )
    }

    @Test
    fun opmlDocument_serializes() = runTest {
        "Tech".also { folderName ->
            feedFixture.create(
                title = "The Verge",
                feedURL = "https://www.theverge.com/rss/index.xml"
            ).let { tagFixture.create(name = folderName, feed = it) }

            feedFixture.create(
                title = "Ars Technica",
                feedURL = "https://feeds.arstechnica.com/arstechnica/index"
            ).let { tagFixture.create(name = folderName, feed = it) }
        }

        "Programming".also { folderName ->
            feedFixture.create(
                title = "De Programmatica Ipsum",
                feedURL = "https://deprogrammaticaipsum.com/feed"
            ).let { tagFixture.create(name = folderName, feed = it) }

            feedFixture.create(
                title = "Ruby Weekly",
                feedURL = "https://cprss.s3.amazonaws.com/rubyweekly.com.xml"
            ).let { tagFixture.create(name = folderName, feed = it) }

            feedFixture.create(
                title = "R&A Enterprise Architecture",
                feedURL = "https://ea.rna.nl/feed/"
            ).let { tagFixture.create(name = folderName, feed = it) }
        }

        feedFixture.create(
            feedURL = "https://gamersnexus.net/rss.xml",
            title = "GamersNexus",
        )

        feedFixture.create(
            feedURL = "https://9to5google.com/feed",
            title = "9to5Google",
        )

        val opmlFile = OPMLFile(account = account)

        val documentSnapshot = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- OPML generated by Capy Reader -->
            <opml version="1.1">
              <head>
                <title>Capy Reader Export</title>
              </head>
              <body>
                <outline text="9to5Google" title="9to5Google" description="" type="rss" version="RSS" htmlUrl="https://9to5google.com/feed" xmlUrl="https://9to5google.com/feed"/>
                <outline text="GamersNexus" title="GamersNexus" description="" type="rss" version="RSS" htmlUrl="https://gamersnexus.net/rss.xml" xmlUrl="https://gamersnexus.net/rss.xml"/>
                <outline text="Programming" title="Programming">
                  <outline text="De Programmatica Ipsum" title="De Programmatica Ipsum" description="" type="rss" version="RSS" htmlUrl="https://deprogrammaticaipsum.com/feed" xmlUrl="https://deprogrammaticaipsum.com/feed"/>
                  <outline text="R&amp;A Enterprise Architecture" title="R&amp;A Enterprise Architecture" description="" type="rss" version="RSS" htmlUrl="https://ea.rna.nl/feed/" xmlUrl="https://ea.rna.nl/feed/"/>
                  <outline text="Ruby Weekly" title="Ruby Weekly" description="" type="rss" version="RSS" htmlUrl="https://cprss.s3.amazonaws.com/rubyweekly.com.xml" xmlUrl="https://cprss.s3.amazonaws.com/rubyweekly.com.xml"/>
                </outline>
                <outline text="Tech" title="Tech">
                  <outline text="Ars Technica" title="Ars Technica" description="" type="rss" version="RSS" htmlUrl="https://feeds.arstechnica.com/arstechnica/index" xmlUrl="https://feeds.arstechnica.com/arstechnica/index"/>
                  <outline text="The Verge" title="The Verge" description="" type="rss" version="RSS" htmlUrl="https://www.theverge.com/rss/index.xml" xmlUrl="https://www.theverge.com/rss/index.xml"/>
                </outline>
              </body>
            </opml>
        """.trimIndent()

        assertEquals(opmlFile.opmlDocument(), documentSnapshot)
    }
}
