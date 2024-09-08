package com.capyreader.app.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneMotion
import androidx.compose.material3.adaptive.layout.PaneScaffoldMotionScope

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class FadePaneMotion: PaneMotion {
    override val PaneScaffoldMotionScope.enterTransition: EnterTransition
        get() = fadeIn()
    override val PaneScaffoldMotionScope.exitTransition: ExitTransition
        get() = fadeOut()
}
