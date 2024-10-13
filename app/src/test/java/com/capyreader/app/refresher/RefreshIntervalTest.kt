package com.capyreader.app.refresher

import com.capyreader.app.refresher.RefreshInterval.EVERY_12_HOURS
import com.capyreader.app.refresher.RefreshInterval.MANUALLY_ONLY
import com.capyreader.app.refresher.RefreshInterval.ON_START
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshIntervalTest {
    @Test
    fun isPeriodic_manual() {
       assertFalse(MANUALLY_ONLY.isPeriodic)
    }

    @Test
    fun isPeriodic_onStart() {
        assertFalse(ON_START.isPeriodic)
    }

    @Test
    fun isPeriodic_periodic() {
        assertTrue(EVERY_12_HOURS.isPeriodic)
    }
}
