/*
 * Created by Josiah Campbell.
 */
package com.jocmp.newsblurclient

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsBlurLoginResponse(
    val code: Int,
    val errors: NewsBlurLoginErrors? = null,
) {
    val authenticated: Boolean
        get() = code == 1
}

@JsonClass(generateAdapter = true)
data class NewsBlurLoginErrors(
    val username: List<String>? = null,
    @Json(name = "__all__") val others: List<String>? = null,
)
