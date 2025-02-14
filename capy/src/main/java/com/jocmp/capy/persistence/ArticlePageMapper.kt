package com.jocmp.capy.persistence

import com.jocmp.capy.ArticlePages

fun articlePageMapper(
    previousID: String?,
    currentIndex: Long,
    nextID: String?,
    size: Long,
) =
    ArticlePages(
        previousID,
        current = currentIndex.toInt(),
        nextID,
        size.toInt(),
    )
