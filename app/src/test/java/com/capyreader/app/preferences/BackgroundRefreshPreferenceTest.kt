package com.capyreader.app.preferences

import androidx.preference.PreferenceManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class BackgroundRefreshPreferenceTest {
    private val context get() = RuntimeEnvironment.getApplication()

    private val appPreferences get() = AppPreferences(context)

    @Before
    fun setUp() {
        appPreferences.clearAll()
    }

    @Test
    fun backgroundRefresh_enabledByDefault() {
        assertTrue(appPreferences.backgroundRefresh.get())
    }

    @Test
    fun backgroundRefresh_legacyManualOnly_disabled() {
        storeLegacyInterval("MANUALLY_ONLY")

        assertFalse(appPreferences.backgroundRefresh.get())
    }

    @Test
    fun backgroundRefresh_legacyPeriodicInterval_enabled() {
        storeLegacyInterval("EVERY_DAY")

        assertTrue(appPreferences.backgroundRefresh.get())
    }

    @Test
    fun backgroundRefresh_explicitValueOverridesLegacy() {
        storeLegacyInterval("MANUALLY_ONLY")

        appPreferences.backgroundRefresh.set(true)

        assertTrue(appPreferences.backgroundRefresh.get())
    }

    private fun storeLegacyInterval(value: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString("refresh_interval", value)
            .commit()
    }
}
