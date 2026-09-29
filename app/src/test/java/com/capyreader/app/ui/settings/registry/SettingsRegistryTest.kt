package com.capyreader.app.ui.settings.registry

import com.capyreader.app.ui.settings.panels.SettingsPanel
import com.jocmp.capy.accounts.Source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SettingsRegistryTest {
    private val local = SettingsEnvironment(
        source = Source.LOCAL,
        crashReporting = false,
        debug = false,
    )

    private val feedbin = local.copy(source = Source.FEEDBIN)

    private val resolve = { id: Int -> RuntimeEnvironment.getApplication().getString(id) }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `places every setting in exactly one panel`() {
        val placed = SettingsPanel.items.flatMap { panel ->
            SettingsRegistry.declaredSections(panel).flatMap { it.settings }
        }

        assertEquals(Setting.entries.sorted(), placed.sorted())
    }

    @Test
    fun `local accounts hide service rows and show local rows`() {
        val settings = accountSettings(local)

        assertFalse(settings.contains(Setting.ACCOUNT))
        assertFalse(settings.contains(Setting.SERVER))
        assertTrue(settings.contains(Setting.OPML_IMPORT))
        assertTrue(articleListSettings(local).contains(Setting.FILTERS))
    }

    @Test
    fun `service accounts hide local rows and show service rows`() {
        val settings = accountSettings(feedbin)

        assertTrue(settings.contains(Setting.ACCOUNT))
        assertTrue(settings.contains(Setting.SERVER))
        assertFalse(settings.contains(Setting.OPML_IMPORT))
        assertFalse(articleListSettings(feedbin).contains(Setting.FILTERS))
    }

    @Test
    fun `drops sections with no available settings`() {
        val titles = SettingsRegistry.sections(SettingsPanel.Advanced, local).map { it.title }

        assertEquals(2, titles.size)
    }

    @Test
    fun `blank query returns no results`() {
        assertTrue(SettingsRegistry.search("  ", local, resolve).isEmpty())
    }

    @Test
    fun `search includes the path to each setting`() {
        val results = SettingsRegistry.search("swipe up", local, resolve)
            .filter { it.setting != null }
            .map { it.setting to it.path.joinToString(" > ") }

        assertEquals(
            listOf(
                Setting.LIST_SWIPE_UP to "Article List > Gestures",
                Setting.READER_SWIPE_UP to "Reader > Gestures",
            ),
            results,
        )
    }

    @Test
    fun `search matches panel titles`() {
        val result = SettingsRegistry.search("reader", local, resolve).first()

        assertEquals(SettingsPanel.Reader, result.panel)
        assertEquals(null, result.setting)
        assertTrue(result.path.isEmpty())
    }

    @Test
    fun `search matches keywords`() {
        val settings = SettingsRegistry.search("oldest", local, resolve).map { it.setting }

        assertEquals(listOf(Setting.SORT_ORDER), settings)
    }

    @Test
    fun `search skips unavailable settings`() {
        val settings = SettingsRegistry.search("filters", feedbin, resolve).map { it.setting }

        assertFalse(settings.contains(Setting.FILTERS))
    }

    private fun accountSettings(environment: SettingsEnvironment) =
        SettingsRegistry.sections(SettingsPanel.Account, environment).flatMap { it.settings }

    private fun articleListSettings(environment: SettingsEnvironment) =
        SettingsRegistry.sections(SettingsPanel.ArticleList, environment).flatMap { it.settings }
}
