package com.jocmp.capy.common

import com.jocmp.capy.SavedSearch

internal fun List<SavedSearch>.sortedByName() =
    sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
