package com.jocmp.basil.accounts

import kotlinx.serialization.Serializable

@Serializable
internal data class SubscriptionChoice(
    val feed_url: String,
    val title: String,
)
