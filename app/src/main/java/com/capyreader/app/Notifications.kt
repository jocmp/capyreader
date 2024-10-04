package com.capyreader.app

enum class Notifications(val channelID: String) {
    OPML_IMPORT(channelID = "opml_import"),

    FEED_UPDATE(channelID = "feed_update")
}
