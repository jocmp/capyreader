package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.capyreader.app.preferences.AppPreferences

class ControlsSettingsViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {
    var scrollUpKeyCode by mutableIntStateOf(controlsOptions.scrollUpKeyCode.get())
        private set

    var scrollDownKeyCode by mutableIntStateOf(controlsOptions.scrollDownKeyCode.get())
        private set

    var previousArticleKeyCode by mutableIntStateOf(controlsOptions.previousArticleKeyCode.get())
        private set

    var nextArticleKeyCode by mutableIntStateOf(controlsOptions.nextArticleKeyCode.get())
        private set

    var toggleStarKeyCode by mutableIntStateOf(controlsOptions.toggleStarKeyCode.get())
        private set

    var scrollDistancePercent by mutableFloatStateOf(controlsOptions.scrollDistancePercent.get())
        private set

    fun updateScrollUpKeyCode(keyCode: Int) {
        scrollUpKeyCode = keyCode
        controlsOptions.scrollUpKeyCode.set(keyCode)
    }

    fun updateScrollDownKeyCode(keyCode: Int) {
        scrollDownKeyCode = keyCode
        controlsOptions.scrollDownKeyCode.set(keyCode)
    }

    fun updatePreviousArticleKeyCode(keyCode: Int) {
        previousArticleKeyCode = keyCode
        controlsOptions.previousArticleKeyCode.set(keyCode)
    }

    fun updateNextArticleKeyCode(keyCode: Int) {
        nextArticleKeyCode = keyCode
        controlsOptions.nextArticleKeyCode.set(keyCode)
    }

    fun updateToggleStarKeyCode(keyCode: Int) {
        toggleStarKeyCode = keyCode
        controlsOptions.toggleStarKeyCode.set(keyCode)
    }

    fun updateScrollDistancePercent(percent: Float) {
        scrollDistancePercent = percent
        controlsOptions.scrollDistancePercent.set(percent)
    }

    private val controlsOptions: AppPreferences.ControlsOptions
        get() = appPreferences.controlsOptions
}
