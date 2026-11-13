package com.jocmp.capy.articles

data class StatusChange(
    val articleIDs: List<String>,
    val read: Boolean? = null,
    val starred: Boolean? = null,
)
