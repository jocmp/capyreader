package com.jocmp.capy.common

import com.jocmp.capy.Folder

fun List<Folder>.sortedByTitle() =
    sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
