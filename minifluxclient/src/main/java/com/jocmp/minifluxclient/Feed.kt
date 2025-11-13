package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Feed(
    val id: Long,
    val user_id: Long,
    val title: String,
    val site_url: String,
    val feed_url: String,
    val checked_at: String,
    val etag_header: String?,
    val last_modified_header: String?,
    val parsing_error_message: String?,
    val parsing_error_count: Int,
    val scraper_rules: String?,
    val rewrite_rules: String?,
    val crawler: Boolean,
    val blocklist_rules: String?,
    val keeplist_rules: String?,
    val user_agent: String?,
    val username: String?,
    val password: String?,
    val disabled: Boolean,
    val ignore_http_cache: Boolean,
    val fetch_via_proxy: Boolean,
    val category: Category?,
    val icon: Icon?
)
