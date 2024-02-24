package com.jocmp.basil

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class OPMLFileTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    @Test
    fun opmlDocument_serializes() {
        val accountPath = folder.newFile().toURI()
        val account = Account(
            id = "777",
            path = accountPath.resolve("test.opml"),
            database = InMemoryDatabaseProvider.build("777"),
            preferences = AccountPreferences(InMemoryDataStore())
        )

        account.folders.addAll(
            listOf(
                Folder(title = "Empty Folder"),
                Folder(
                    title = "Tech",
                    feeds = mutableListOf(
                        Feed(
                            id = "1",
                            subscriptionID = RandomUUID.generate(),
                            name = "The Verge",
                            feedURL = "https://www.theverge.com/rss/index.xml"
                        ),
                        Feed(
                            id = "2",
                            subscriptionID = RandomUUID.generate(),
                            name = "Ars Technica",
                            feedURL = "https://feeds.arstechnica.com/arstechnica/index"
                        )
                    )
                ),
                Folder(
                    title = "Programming",
                    feeds = mutableListOf(
                        Feed(
                            id = "3",
                            subscriptionID = RandomUUID.generate(),
                            name = "De Programmatica Ipsum",
                            feedURL = "https://deprogrammaticaipsum.com/feed"
                        ),
                        Feed(
                            id = "4",
                            subscriptionID = RandomUUID.generate(),
                            name = "Ruby Weekly",
                            feedURL = "https://cprss.s3.amazonaws.com/rubyweekly.com.xml"
                        ),
                        Feed(
                            id = "10",
                            subscriptionID = RandomUUID.generate(),
                            name = "R&A Enterprise Architecture",
                            feedURL = "https://ea.rna.nl/feed/"
                        ),
                    )
                )
            )
        )

        account.feeds.addAll(
            listOf(
                Feed(
                    id = "5",
                    subscriptionID = RandomUUID.generate(),
                    name = "GamersNexus",
                    feedURL = "https://gamersnexus.net/rss.xml"
                ),
                Feed(
                    id = "6",
                    subscriptionID = RandomUUID.generate(),
                    name = "9to5Google",
                    feedURL = "https://9to5google.com/feed"
                )
            )
        )

        val opmlFile = OPMLFile(
            path = accountPath,
            account = account
        )

        val documentSnapshot = """
        |<?xml version="1.0" encoding="UTF-8"?>
        |<!-- OPML generated by Basil Reader -->
        |<opml version="1.1">
        |  <head>
        |    <title>Feedbin</title>
        |  </head>
        |  <body>
        |    <outline text="9to5Google" title="9to5Google" description="" type="rss" version="RSS" htmlUrl="" xmlUrl="https://9to5google.com/feed" basil_id="6"/>
        |    <outline text="GamersNexus" title="GamersNexus" description="" type="rss" version="RSS" htmlUrl="" xmlUrl="https://gamersnexus.net/rss.xml" basil_id="5"/>
        |    <outline text="Empty Folder" title="Empty Folder"/>
        |    <outline text="Programming" title="Programming">
        |      <outline text="De Programmatica Ipsum" title="De Programmatica Ipsum" description="" type="rss" version="RSS" htmlUrl="" xmlUrl="https://deprogrammaticaipsum.com/feed" basil_id="3"/>
        |      <outline text="Ruby Weekly" title="Ruby Weekly" description="" type="rss" version="RSS" htmlUrl="" xmlUrl="https://cprss.s3.amazonaws.com/rubyweekly.com.xml" basil_id="4"/>
        |      <outline text="R&amp;A Enterprise Architecture" title="R&amp;A Enterprise Architecture" description="" type="rss" version="RSS" htmlUrl="" xmlUrl="https://ea.rna.nl/feed/" basil_id="10"/>
        |    </outline>
        |    <outline text="Tech" title="Tech">
        |      <outline text="The Verge" title="The Verge" description="" type="rss" version="RSS" htmlUrl="" xmlUrl="https://www.theverge.com/rss/index.xml" basil_id="1"/>
        |      <outline text="Ars Technica" title="Ars Technica" description="" type="rss" version="RSS" htmlUrl="" xmlUrl="https://feeds.arstechnica.com/arstechnica/index" basil_id="2"/>
        |    </outline>
        |  </body>
        |</opml>
        """.trimMargin()

        assertEquals(opmlFile.opmlDocument(), documentSnapshot)
    }
}
