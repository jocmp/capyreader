package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val code: Int,
    val authenticated: Boolean = false,
    val errors: Map<String, List<String>>? = null,
) {
    val errorMessage: String?
        get() = errors?.values?.flatten()?.firstOrNull()
}
