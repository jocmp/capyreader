package com.capyreader.app.ui

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions

sealed class Route(val path: String) {
    data object AddAccount : Route("add-account")

    data object Login : Route("login")

    data object Settings : Route("settings")

    data object Articles : Route("articles")
}
