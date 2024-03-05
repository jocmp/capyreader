package com.jocmp.basil

class AccountTest {
//    @JvmField
//    @Rule
//    val folder = TemporaryFolder()
//
//    private lateinit var database: Database
//
//    private val defaultEntry = AddFeedForm(
//        url = URL(THE_VERGE_URL),
//        name = "The Verge",
//        folderTitles = listOf()
//    )
//
//    private val feedFinder = TestFeedFinder(
//        mapOf(
//            THE_VERGE_URL to TheVergeFeed(),
//            ARS_TECHNICA_URL to ArsTechnicaFeed()
//        )
//    )
//
//    @Before
//    fun setup() {
//        mockkConstructor(aFeedbinAccountDelegate::class)
//
//        coEvery {
//            anyConstructed<FeedbinAccountDelegate>().fetchAll(any())
//        } returns emptyList()
//
//        database = InMemoryDatabaseProvider.build("777")
//    }
//
//    private fun buildAccount(id: String = "777", path: File = folder.newFile()): Account {
//        return Account(
//            id = id,
//            path = path.toURI(),
//            database = database,
//            preferences = AccountPreferences(InMemoryDataStore()),
//            feedFinder = feedFinder
//        )
//    }
//
//    @Test
//    fun constructor_loadsExistingFeeds() = runTest {
//        val accountPath = folder.newFile()
//        val accountID = "777"
//
//        val previousInstance = buildAccount(id = accountID, path = accountPath)
//        previousInstance.addFeed(
//            AddFeedForm(
//                url = URL(THE_VERGE_URL),
//                name = "The Verge",
//                folderTitles = listOf("Test Title"),
//            )
//        )
//
//        val account = buildAccount(id = accountID, path = accountPath)
//        val accountTitle = account.folders.first().first().title
//
//        assertEquals(expected = "Test Title", actual = accountTitle)
//        assertEquals(expected = 1, actual = account.flattenedFeeds.size)
//    }
//
//    @Test
//    fun addFeed_singleTopLevelFeed() = runTest {
//        val accountPath = folder.newFile()
//        val account = buildAccount(id = "777", path = accountPath)
//        val entry = AddFeedForm(
//            url = URL("https://theverge.com/rss/index.xml"),
//            name = "The Verge",
//            folderTitles = listOf(),
//        )
//
//        account.addFeed(entry)
//
//        assertEquals(expected = account.feeds.first().size, actual = 1)
//        assertEquals(expected = account.folders.first().size, actual = 0)
//
//        val feed = account.feeds.first().first()
//        assertEquals(expected = entry.name, actual = entry.name)
//        assertEquals(expected = entry.url.toString(), actual = feed.feedURL)
//    }
//
//    @Test
//    fun addFeed_newFolder() {
//        val accountPath = folder.newFile()
//        val account = buildAccount(id = "777", path = accountPath)
//        val entry = AddFeedForm(
//            url = URL("https://theverge.com/rss/index.xml"),
//            name = "The Verge",
//            folderTitles = listOf("Tech"),
//        )
//
//        runBlocking { account.addFeed(entry) }
//
//        assertEquals(expected = account.topLevelFeeds.size, actual = 0)
//        assertEquals(expected = account.folders.size, actual = 1)
//
//        val feed = account.folders.first().feeds.first()
//        assertEquals(expected = entry.name, actual = entry.name)
//        assertEquals(expected = entry.url.toString(), actual = feed.feedURL)
//    }
//
//    @Test
//    fun addFeed_existingFolders() {
//        val accountPath = folder.newFile()
//        val account = buildAccount(id = "777", path = accountPath)
//        runBlocking { account.addFolder("Tech") }
//
//        val entry = AddFeedForm(
//            url = URL("https://theverge.com/rss/index.xml"),
//            name = "The Verge",
//            folderTitles = listOf("Tech"),
//        )
//
//        runBlocking { account.addFeed(entry) }
//
//        assertEquals(expected = account.topLevelFeeds.size, actual = 0)
//        assertEquals(expected = account.folders.size, actual = 1)
//
//        val feed = account.folders.first().feeds.first()
//        assertEquals(expected = entry.name, actual = feed.name)
//        assertEquals(expected = entry.url.toString(), actual = feed.feedURL)
//    }
//
//    @Test
//    fun addFeed_multipleFolders() {
//        val accountPath = folder.newFile()
//        val account = buildAccount(id = "777", path = accountPath)
//        runBlocking { account.addFolder("Tech") }
//
//        val entry = AddFeedForm(
//            url = URL("https://theverge.com/rss/index.xml"),
//            name = "The Verge",
//            folderTitles = listOf("Tech", "Culture"),
//        )
//
//        runBlocking { account.addFeed(entry) }
//
//        assertEquals(expected = account.topLevelFeeds.size, actual = 0)
//        assertEquals(expected = account.folders.size, actual = 2)
//
//        val techFeed = account.folders.first().feeds.first()
//        val cultureFeed = account.folders.first().feeds.first()
//        assertEquals(expected = entry.name, actual = techFeed.name)
//        assertEquals(expected = entry.url.toString(), actual = techFeed.feedURL)
//        assertEquals(techFeed, cultureFeed)
//    }
//
//    @Test
//    fun removeFeed_topLevelFeed() {
//        val account = buildAccount()
//        runBlocking {
//            account.addFeed(
//                AddFeedForm(
//                    url = URL("https://theverge.com/rss/index.xml"),
//                    name = "The Verge",
//                    folderTitles = listOf(),
//                )
//            )
//        }
//
//        val feed = account.topLevelFeeds.find { it.name == "The Verge" }!!
//
//        assertEquals(expected = 1, account.topLevelFeeds.size)
//
//        runBlocking { account.removeFeed(feedID = feed.id) }
//
//        assertEquals(expected = 0, account.topLevelFeeds.size)
//    }
//
//    @Test
//    fun editFeed_topLevelFeed() {
//        val account = buildAccount()
//        val feed = runBlocking { account.addFeed(defaultEntry) }.getOrNull()!!
//
//        val feedName = "The Verge Mobile"
//
//        val editedFeed = runBlocking {
//            account.editFeed(EditFeedForm(feedID = feed.id, name = feedName))
//        }.getOrNull()!!
//
//        assertEquals(expected = feedName, actual = editedFeed.name)
//    }
//
//    @Test
//    fun editFeed_nestedFeed() {
//        val account = buildAccount()
//        val feed = runBlocking {
//            account.addFeed(defaultEntry.copy(folderTitles = listOf("Tech")))
//        }.getOrNull()!!
//
//        val feedName = "The Verge Mobile"
//
//        runBlocking {
//            account.editFeed(
//                EditFeedForm(
//                    feedID = feed.id,
//                    name = feedName,
//                    folderTitles = listOf("Tech")
//                )
//            )
//        }
//
//        val renamedFeed = account.folders.first().feeds.first()
//
//        assertEquals(expected = feedName, actual = renamedFeed.name)
//    }
//
//    @Test
//    fun editFeed_movedFeedFromTopLevelToFolder() {
//        val account = buildAccount()
//        val feed = runBlocking {
//            account.addFeed(defaultEntry)
//        }.getOrNull()!!
//
//        val feedName = "The Verge Mobile"
//
//        runBlocking {
//            account.editFeed(
//                EditFeedForm(
//                    feedID = feed.id,
//                    name = feedName,
//                    folderTitles = listOf("Tech")
//                )
//            )
//        }
//
//        assertTrue(account.topLevelFeeds.isEmpty())
//        assertEquals(expected = 1, actual = account.folders.size)
//
//        val renamedFeed = account.folders.first().feeds.first()
//
//        assertEquals(expected = feedName, actual = renamedFeed.name)
//    }
//
//    @Test
//    fun editFeed_movedFeedFromFolderToTopLevel() {
//        val account = buildAccount()
//
//        val feed = runBlocking {
//            account.addFeed(defaultEntry)
//        }.getOrNull()!!
//
//        val otherFeed = runBlocking {
//            account.addFeed(
//                AddFeedForm(
//                    url = URL(ARS_TECHNICA_URL),
//                    name = "Ars Technica",
//                    folderTitles = listOf("Tech")
//                )
//            )
//        }.getOrNull()!!
//
//        val feedName = "The Verge"
//
//        runBlocking {
//            account.editFeed(
//                EditFeedForm(
//                    feedID = feed.id,
//                    name = feedName,
//                    folderTitles = listOf()
//                )
//            )
//        }
//
//        assertEquals(expected = 1, actual = account.topLevelFeeds.size)
//        assertEquals(expected = 1, actual = account.folders.size)
//
//        val movedFeed = account.topLevelFeeds.first()
//        val existingFeed = account.folders.first().feeds.first()
//
//        assertEquals(expected = feedName, actual = movedFeed.name)
//        assertEquals(expected = otherFeed.name, actual = existingFeed.name)
//    }
//
//    @Test
//    fun editFeed_movedFeedFromFolderToTopLevelWithOtherFeeds() {
//        val account = buildAccount()
//        val feed = runBlocking {
//            account.addFeed(defaultEntry)
//        }.getOrNull()!!
//
//        val feedName = "The Verge Mobile"
//
//        runBlocking {
//            account.editFeed(
//                EditFeedForm(
//                    feedID = feed.id,
//                    name = feedName,
//                    folderTitles = listOf("Tech")
//                )
//            )
//        }
//
//        assertTrue(account.topLevelFeeds.isEmpty())
//        assertEquals(expected = 1, actual = account.folders.size)
//
//        val renamedFeed = account.folders.first().feeds.first()
//
//        assertEquals(expected = feedName, actual = renamedFeed.name)
//    }
//
//    @Test
//    fun editFolder() {
//        val account = buildAccount()
//
//        runBlocking {
//            account.addFeed(defaultEntry.copy(folderTitles = listOf("Tech")))
//        }
//
//        val folderTitle = "Tech & Culture"
//
//        runBlocking {
//            account.editFolder(form = EditFolderForm(existingTitle = "Tech", title = folderTitle))
//        }
//
//        assertEquals(expected = 1, actual = account.folders.size)
//
//        val renamedFolder = account.folders.first()
//
//        assertEquals(expected = folderTitle, actual = renamedFolder.title)
//    }
//
//    @Test
//    fun removeFolder() {
//        val account = buildAccount()
//
//        val feed = runBlocking {
//            account.addFeed(defaultEntry.copy(folderTitles = listOf("Tech")))
//        }.getOrNull()!!
//
//        runBlocking {
//            account.removeFolder(title = "Tech")
//        }
//
//        assertEquals(expected = 0, actual = account.folders.size)
//        assertEquals(expected = 1, actual = account.topLevelFeeds.size)
//
//        val movedFeed = account.topLevelFeeds.first()
//
//        assertEquals(expected = movedFeed.id, actual = feed.id)
//    }
//
//    @Test
//    fun removeFolder_feedInMultipleFolders() {
//        val account = buildAccount()
//
//        val feed = runBlocking {
//            account.addFeed(defaultEntry.copy(folderTitles = listOf("Tech", "Culture")))
//        }.getOrNull()!!
//
//        runBlocking {
//            account.removeFolder(title = "Tech")
//        }
//
//        assertEquals(expected = 1, actual = account.folders.size)
//        assertEquals(expected = 0, actual = account.topLevelFeeds.size)
//
//        val otherFolder = account.folders.first()
//        val movedFeed = otherFolder.feeds.first()
//
//        assertEquals(expected = "Culture", otherFolder.title)
//        assertEquals(expected = movedFeed.id, actual = feed.id)
//    }
//
//    @Test
//    fun findFeed_topLevelFeed() {
//        val account = buildAccount(id = "777", path = folder.newFile())
//
//        val entry = AddFeedForm(
//            url = URL("https://theverge.com/rss/index.xml"),
//            name = "The Verge",
//            folderTitles = emptyList()
//        )
//
//        val feedID = runBlocking { account.addFeed(entry) }.getOrNull()!!.id
//
//        val result = account.findFeed(feedID)!!
//
//        assertEquals(expected = feedID, actual = result.id)
//    }
//
//    @Test
//    fun findFeed_nestedFeed() {
//        val account = buildAccount(id = "777", path = folder.newFile())
//
//        val entry = defaultEntry.copy(folderTitles = listOf("Tech", "Culture"))
//
//        val feedID = runBlocking { account.addFeed(entry) }.getOrNull()!!.id
//
//        val result = account.findFeed(feedID)!!
//
//        assertEquals(expected = feedID, actual = result.id)
//    }
//
//    @Test
//    fun findFeed_feedDoesNotExist() {
//        val account = buildAccount(id = "777", path = folder.newFile())
//
//        val result = account.findFeed("missing")
//
//        assertNull(result)
//    }
//
//    @Test
//    fun findFolder_existingFolder() {
//        val account = buildAccount(id = "777", path = folder.newFile())
//
//        val entry = AddFeedForm(
//            url = URL("https://theverge.com/rss/index.xml"),
//            name = "The Verge",
//            folderTitles = listOf("Tech", "Culture")
//        )
//
//        runBlocking { account.addFeed(entry) }
//
//        val result = account.findFolder("Tech")!!
//
//        assertEquals(expected = "Tech", actual = result.title)
//    }
//
//    @Test
//    fun findFolder_folderDoesNotExist() {
//        val account = buildAccount(id = "777", path = folder.newFile())
//
//        val result = account.findFolder("Tech")
//
//        assertNull(result)
//    }
}
