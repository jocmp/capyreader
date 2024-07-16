package com.jocmp.capy.articles

import com.jocmp.capy.common.Async

data class ExtractedContent(
    val requestShow: Boolean = false,
    val value: Async<String?> = Async.Uninitialized,
) {
    val showByDefault: Boolean
        get() = requestShow && value is Async.Uninitialized

    val isLoading: Boolean
        get() = requestShow && value is Async.Loading

    val isComplete: Boolean
        get() = requestShow && value is Async.Success

    val showOnLoad: Boolean
        get() = requestShow && value is Async.Uninitialized
}
