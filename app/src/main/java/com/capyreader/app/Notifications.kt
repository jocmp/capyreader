package com.capyreader.app

import android.os.Build

enum class Notifications(val channelID: String) {
    OPML_IMPORT(channelID = "opml_import"),

    FEED_UPDATE(channelID = "feed_update");

    companion object {
        val askForPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}
