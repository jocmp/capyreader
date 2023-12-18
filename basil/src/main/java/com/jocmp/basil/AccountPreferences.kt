package com.jocmp.basil

import kotlinx.serialization.Serializable

@Serializable
data class AccountPreferences(
    val displayName: String
)
