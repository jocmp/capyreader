package com.jocmp.capyreader.ui

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions

sealed class Route(val path: String) {
    data object AddAccount : Route("add-account")

    data object Login : Route("login")

    data object Settings : Route("settings")

    data object Articles : Route("articles")
}

fun NavController.navigate(route: Route) = navigate(route.path)

fun NavController.navigate(route: Route, builder: NavOptionsBuilder.() -> Unit) {
    navigate(route.path, navOptions(builder))
}
