package com.capyreader.app.ui

import android.os.Build

object EdgeToEdgeHelper {
    /**
     * Attempts to solve issues on MIUI where the share sheet will jump
     * due to the edge-to-edge mode.
     *
     * MIUI 14 is built against Android 12 and 13, so any check below
     * UPSIDE_DOWN_CAKE (SDK 34/Android 14) will suffice.
     */
    fun isEdgeToEdgeAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }
}