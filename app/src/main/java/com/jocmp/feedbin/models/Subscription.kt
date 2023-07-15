package com.jocmp.feedbin.models

import com.squareup.moshi.JsonClass

/**
 * <https://github.com/feedbin/feedbin-api/blob/master/content/subscriptions.md>
 *
 * @sample
 * Get Subscription
 * GET /v2/subscriptions/525.json will return the feed with an id of 525
 * ```json
 * {
 *  "id": 525,
 *  "created_at": "2013-03-12T11:30:25.209432Z",
 *  "feed_id": 47,
 *  "title": "Daring Fireball",
 *  "feed_url": "http://daringfireball.net/index.xml",
 *  "site_url": "http://daringfireball.net/"
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class Subscription(
    val id: String,
    val created_at: String,
    val feed_id: Int,
    val title: String,
    val feed_url: String,
    val site_url: String
)