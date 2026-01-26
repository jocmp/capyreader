package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateFeedRequest(
    val title: String? = null,
    val category_id: Long? = null,
    val scraper_rules: String? = null,
    val rewrite_rules: String? = null,
    val blocklist_rules: String? = null,
    val keeplist_rules: String? = null,
    val crawler: Boolean? = null,
    val user_agent: String? = null,
    val username: String? = null,
    val password: String? = null,
    val disabled: Boolean? = null,
    val ignore_http_cache: Boolean? = null,
    val fetch_via_proxy: Boolean? = null
)
