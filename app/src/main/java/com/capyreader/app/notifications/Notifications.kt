package com.capyreader.app.notifications

import android.os.Build

enum class Notifications(val channelID: String) {
    OPML_IMPORT(channelID = "opml_import"),

    FEED_UPDATE(channelID = "feed_update");

    companion object {
        val askForPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        const val OPML_IMPORT_NOTIFICATION_ID = 6_170_000

        const val FEED_UPDATE_GROUP_NOTIFICATION_ID = 6_170_100
    }
}
