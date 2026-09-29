package com.capyreader.app.preferences

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class EInkDeviceTest {
    private lateinit var appPreferences: AppPreferences

    @Before
    fun setUp() {
        appPreferences = AppPreferences(RuntimeEnvironment.getApplication()).also {
            it.clearAll()
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `detects Boox devices`() {
        assertTrue(EInkDevice.isEInk(manufacturer = "ONYX", brand = "Boox", model = "Go7"))
    }

    @Test
    fun `detects E Ink models from general manufacturers`() {
        assertTrue(EInkDevice.isEInk(manufacturer = "Xiaomi", brand = "Xiaomi", model = "InkPalm 5"))
    }

    @Test
    fun `ignores phones`() {
        assertFalse(EInkDevice.isEInk(manufacturer = "Google", brand = "google", model = "Pixel 9"))
    }

    @Test
    fun `applies the E Ink bundle once`() {
        EInkDevice.applyDefaults(appPreferences, isEInk = true)

        assertTrue(appPreferences.reduceMotion.get())
        assertTrue(appPreferences.readerOptions.enablePagingTapGesture.get())
        assertTrue(appPreferences.readerOptions.enablePageTurnKeys.get())

        appPreferences.reduceMotion.set(false)
        EInkDevice.applyDefaults(appPreferences, isEInk = true)

        assertFalse(appPreferences.reduceMotion.get())
    }

    @Test
    fun `leaves other devices alone`() {
        EInkDevice.applyDefaults(appPreferences, isEInk = false)

        assertFalse(appPreferences.reduceMotion.get())
        assertFalse(appPreferences.readerOptions.enablePageTurnKeys.get())
        assertFalse(appPreferences.eInkDefaultsApplied.get())
    }
}
