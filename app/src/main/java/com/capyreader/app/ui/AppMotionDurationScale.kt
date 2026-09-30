package com.capyreader.app.ui

import android.content.ContentResolver
import android.provider.Settings
import androidx.compose.ui.MotionDurationScale

class AppMotionDurationScale : MotionDurationScale {
    @Volatile
    var reduceMotion = false

    @Volatile
    private var systemScale = 1f

    override val scaleFactor: Float
        get() {
            if (reduceMotion) {
                return 0f
            }

            return systemScale
        }

    fun refreshSystemScale(contentResolver: ContentResolver) {
        systemScale = Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
    }
}
