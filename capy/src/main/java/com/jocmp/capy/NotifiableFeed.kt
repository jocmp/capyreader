package com.jocmp.capy

data class NotifiableFeed(
    val feed: Feed,
    val articleCount: Long,
) {
    val id = feed.id.hashCode()
}
