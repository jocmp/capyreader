package com.jocmp.rssparser.internal.json.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Item(
    val id: String,
    val url: String?,
    val external_url: String?,
    val title: String?,
    val content_html: String?,
    val content_text: String?,
    val summary: String?,
    val image: String?,
    val banner_image: String?,
    val date_published: String?,
    val date_modified: String?,
    val authors: List<Author>?,
    val tags: List<String>?,
    val language: String?,
)
