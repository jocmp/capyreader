package com.jocmp.rssparser.internal.json.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Feed(
    val version: String,
    val title: String,
    val home_page_url: String?,
    val feed_url: String?,
    val description: String?,
    val user_comment: String?,
    val next_url: String?,
    val icon: String?,
    val favicon: String?,
    val authors: List<Author>?,
    val language: String?,
    val expired: Boolean?,
    val hubs: List<Hub>?,
    val items: List<Item>,
)
