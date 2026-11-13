package com.jocmp.capy.articles

/** A local read or starred write. Null fields are unchanged. */
data class StatusChange(
    val articleIDs: List<String>,
    val read: Boolean? = null,
    val starred: Boolean? = null,
)
