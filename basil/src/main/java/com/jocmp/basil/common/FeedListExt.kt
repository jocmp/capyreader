package com.jocmp.basil.common

import com.jocmp.basil.Feed

internal fun List<Feed>.sortedByTitle() =
    sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
