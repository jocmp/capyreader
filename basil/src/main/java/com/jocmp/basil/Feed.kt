package com.jocmp.basil

import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Serializable
data class Feed(
    val id: String,
    val subscriptionID: String,
    val name: String,
    val feedURL: String,
    val siteURL: String = "",
    override val count: Long = 0
): Countable
