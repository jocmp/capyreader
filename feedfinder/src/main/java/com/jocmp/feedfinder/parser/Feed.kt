package com.jocmp.feedfinder.parser

import java.net.URL

interface Feed {
    fun isValid(): Boolean

    val name: String

    val feedURL: URL

    val siteURL: URL?
}
