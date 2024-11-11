package com.jocmp.capy.accounts

import kotlinx.serialization.Serializable

@Serializable
enum class Source(val value: String) {
    LOCAL("local"),
    FEEDBIN("feedbin"),
    FRESHRSS("freshrss"),
    READER("reader");

    val hasCustomURL
        get() = this == FRESHRSS || this == READER
}
