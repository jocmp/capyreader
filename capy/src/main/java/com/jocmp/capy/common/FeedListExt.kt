package com.jocmp.capy.common

import com.jocmp.capy.Feed

internal fun List<Feed>.sortedByTitle() =
    sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
