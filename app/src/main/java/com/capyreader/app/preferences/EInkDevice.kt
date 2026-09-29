package com.capyreader.app.preferences

import android.os.Build

object EInkDevice {
    fun isEInk(
        manufacturer: String = Build.MANUFACTURER,
        brand: String = Build.BRAND,
        model: String = Build.MODEL,
    ): Boolean {
        val vendors = listOf(manufacturer, brand).map { it.lowercase() }

        if (vendors.any { it in EINK_VENDORS }) {
            return true
        }

        val normalizedModel = model.lowercase()

        return EINK_MODELS.any { normalizedModel.contains(it) }
    }

    fun applyDefaults(appPreferences: AppPreferences, isEInk: Boolean = isEInk()) {
        if (!isEInk || appPreferences.eInkDefaultsApplied.get()) {
            return
        }

        appPreferences.reduceMotion.set(true)
        appPreferences.readerOptions.enablePagingTapGesture.set(true)
        appPreferences.readerOptions.enablePageTurnKeys.set(true)
        appPreferences.eInkDefaultsApplied.set(true)
    }

    private val EINK_VENDORS = setOf(
        "onyx",
        "boox",
        "bigme",
        "meebook",
        "likebook",
        "pocketbook",
        "ratta",
        "supernote",
    )

    private val EINK_MODELS = listOf(
        "inkpalm",
        "e-ink",
        "eink",
    )
}
