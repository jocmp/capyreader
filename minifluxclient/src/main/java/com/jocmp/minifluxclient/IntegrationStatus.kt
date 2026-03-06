package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IntegrationStatus(val has_integrations: Boolean)
