package com.jocmp.minifluxclient

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: Long,
    val username: String,
    @Json(name = "is_admin")
    val isAdmin: Boolean,
    val theme: String,
    val language: String,
    val timezone: String,
    @Json(name = "entry_sorting_direction")
    val entrySortingDirection: String,
    val stylesheet: String,
    @Json(name = "google_id")
    val googleId: String,
    @Json(name = "openid_connect_id")
    val openidConnectId: String,
    @Json(name = "entries_per_page")
    val entriesPerPage: Int,
    @Json(name = "keyboard_shortcuts")
    val keyboardShortcuts: Boolean,
    @Json(name = "show_reading_time")
    val showReadingTime: Boolean,
    @Json(name = "entry_swipe")
    val entrySwipe: Boolean,
    @Json(name = "last_login_at")
    val lastLoginAt: String
)
