package com.jocmp.basilreader.ui

import androidx.navigation.NavController

sealed class Route(val path: String) {
    data object AccountIndex : Route("accounts")

    data object AccountSettings : Route("accounts/{id}/edit") {
        operator fun invoke(id: String) = "accounts/$id/edit"
    }
}

fun NavController.navigate(route: Route) = navigate(route.path)
