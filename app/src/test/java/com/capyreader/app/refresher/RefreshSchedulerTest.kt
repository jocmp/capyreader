package com.capyreader.app.refresher

import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.preferences.Preference
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class RefreshSchedulerTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun initialize_periodicInterval_enqueuesWork() {
        val scheduler = RefreshScheduler(context, appPreferencesWith(RefreshInterval.EVERY_HOUR))

        scheduler.initialize()

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RefreshScheduler.WORK_NAME)
            .get()

        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.first().state)
    }

    @Test
    fun initialize_manualOnly_doesNotEnqueueWork() {
        val scheduler = RefreshScheduler(context, appPreferencesWith(RefreshInterval.MANUALLY_ONLY))

        scheduler.initialize()

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RefreshScheduler.WORK_NAME)
            .get()

        assertTrue(infos.isEmpty())
    }

    @Test
    fun initialize_keepsExistingWork() {
        val scheduler = RefreshScheduler(context, appPreferencesWith(RefreshInterval.EVERY_HOUR))

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

    private fun appPreferencesWith(interval: RefreshInterval): AppPreferences {
        val intervalPreference = mockk<Preference<RefreshInterval>> {
            every { get() } returns interval
        }
        return mockk {
            every { refreshInterval } returns intervalPreference
        }
    }
}
