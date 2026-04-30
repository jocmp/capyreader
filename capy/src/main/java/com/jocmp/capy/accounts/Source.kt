package com.jocmp.capy.accounts

import kotlinx.serialization.Serializable

@Serializable
enum class Source(val value: String) {
    LOCAL("local"),
    FEEDBIN("feedbin"),
    FRESHRSS("freshrss"),

    /** Miniflux with username/password combination */
    MINIFLUX("miniflux"),

    /** Miniflux with API Token */
    MINIFLUX_TOKEN("miniflux_token"),
    READER("reader"),

    /**
     * BazQux Reader — hosted Google Reader-compatible service at bazqux.com.
     * NetNewsWire treats BazQux as a fixed-host generic ReaderAPI variant
     * (NNW: Modules/Account/Sources/Account/ReaderAPI/ReaderAPIVariant.swift).
     * We follow the same pattern, plugging into the shared [ReaderAccountDelegate].
     */
    BAZQUX("bazqux");

    val hasCustomURL
        get() = this == FRESHRSS ||
                this == MINIFLUX ||
                this == MINIFLUX_TOKEN ||
                this == READER ||
                this == BAZQUX

    val requiresUsername
        get() = this != MINIFLUX_TOKEN

    val supportsLabels
        get() = this == FRESHRSS

    /**
     * Miniflux's API with delete all feeds within a category
     * instead of reassigning them. This is surprising and
     * destructive behavior so it's unsupported within the app.
     */
    val supportsTagDeletion
        get() = !isMiniflux

    val supportsReadLater
        get() = this == FEEDBIN

    private val isMiniflux
        get() = this == MINIFLUX || this == MINIFLUX_TOKEN

}
