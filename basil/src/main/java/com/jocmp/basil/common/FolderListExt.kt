package com.jocmp.basil.common

import com.jocmp.basil.Folder

internal fun List<Folder>.sortedByTitle() =
    sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
