package com.jocmp.basilreader.ui

import androidx.navigation.NavController

sealed class Route(val path: String) {
    data object Login : Route("login")

    data object Settings : Route("settings")

    data object Articles : Route("articles")
}

fun NavController.navigate(route: Route) = navigate(route.path)
