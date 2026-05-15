package com.jocmp.readerclient

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Subscription(
    val id: String,
    val title: String,
    val categories: List<Category>,
    val url: String?,
    val htmlUrl: String,
    val iconUrl: String?,
    @Json(name = "frss:priority")
    val frssPriority: String?,
) {
    // BazQux omits "url" on subscriptions. The feed URL is embedded in "id" as "feed/<url>".
    // https://github.com/Ranchero-Software/NetNewsWire/blob/43a06494033c93125a62f37464add7477b3ed664/Modules/Account/Sources/Account/ReaderAPI/ReaderAPISubscription.swift#L79
    val feedURL: String
        get() = url ?: id.removePrefix("feed/")
}
