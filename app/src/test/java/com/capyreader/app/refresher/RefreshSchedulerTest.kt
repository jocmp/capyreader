package com.capyreader.app.refresher

import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.preferences.Preference
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class RefreshSchedulerTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun initialize_enabled_enqueuesWork() {
        val scheduler = RefreshScheduler(context, appPreferencesWith(enabled = true))

        scheduler.initialize()

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RefreshScheduler.WORK_NAME)
            .get()

        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.first().state)
    }

    @Test
    fun initialize_disabled_doesNotEnqueueWork() {
        val scheduler = RefreshScheduler(context, appPreferencesWith(enabled = false))

        scheduler.initialize()

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RefreshScheduler.WORK_NAME)
            .get()

        assertTrue(infos.isEmpty())
    }

    @Test
    fun initialize_keepsExistingWork() {
        val scheduler = RefreshScheduler(context, appPreferencesWith(enabled = true))

        scheduler.initialize()
        val firstId = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RefreshScheduler.WORK_NAME)
            .get()
            .first()
            .id

        scheduler.initialize()
        val secondId = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RefreshScheduler.WORK_NAME)
            .get()
            .first()
            .id

        assertEquals(firstId, secondId)
    }

    @Test
    fun migrateLegacyInterval_updatesExistingWorkToHourly() {
        val appPreferences = freshAppPreferences()
        storeLegacyInterval("EVERY_12_HOURS")
        enqueueExistingWork(repeatIntervalHours = 12)

        RefreshScheduler(context, appPreferences).migrateLegacyInterval()

        assertEquals(TimeUnit.HOURS.toMillis(1), refreshWork().periodicityInfo?.repeatIntervalMillis)
        assertTrue(appPreferences.backgroundRefresh.get())
        assertFalse(appPreferences.legacyRefreshInterval.isSet())
    }

    @Test
    fun migrateLegacyInterval_manualOnly_persistsDisabled() {
        val appPreferences = freshAppPreferences()
        storeLegacyInterval("MANUALLY_ONLY")

        RefreshScheduler(context, appPreferences).migrateLegacyInterval()

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RefreshScheduler.WORK_NAME)
            .get()

        assertTrue(infos.isEmpty())
        assertTrue(appPreferences.backgroundRefresh.isSet())
        assertFalse(appPreferences.backgroundRefresh.get())
        assertFalse(appPreferences.legacyRefreshInterval.isSet())
    }

    @Test
    fun migrateLegacyInterval_withoutLegacyValue_leavesWorkAlone() {
        val appPreferences = freshAppPreferences()
        enqueueExistingWork(repeatIntervalHours = 12)

        RefreshScheduler(context, appPreferences).migrateLegacyInterval()

        assertEquals(TimeUnit.HOURS.toMillis(12), refreshWork().periodicityInfo?.repeatIntervalMillis)
        assertFalse(appPreferences.backgroundRefresh.isSet())
    }

    private fun freshAppPreferences(): AppPreferences {
        return AppPreferences(context).also { it.clearAll() }
    }

    private fun storeLegacyInterval(value: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString("refresh_interval", value)
            .commit()
    }

    private fun enqueueExistingWork(repeatIntervalHours: Long) {
        val request = PeriodicWorkRequestBuilder<RefreshFeedsWorker>(repeatIntervalHours, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(RefreshScheduler.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
            .result
            .get()
    }

    private fun refreshWork(): WorkInfo {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RefreshScheduler.WORK_NAME)
            .get()
            .single()
    }

    private fun appPreferencesWith(enabled: Boolean): AppPreferences {
        val backgroundRefreshPreference = mockk<Preference<Boolean>> {
            every { get() } returns enabled
        }
        return mockk {
            every { backgroundRefresh } returns backgroundRefreshPreference
        }
    }
}
