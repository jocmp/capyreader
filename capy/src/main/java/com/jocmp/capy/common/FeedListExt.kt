package com.jocmp.capy.common

import com.jocmp.capy.Feed
import java.text.Collator
import java.util.Locale

fun List<Feed>.sortedByTitle() =
    sortedWith(compareBy(Collator.getInstance(Locale.getDefault()).apply {
        strength = Collator.PRIMARY
    }) {
        it.title
    })
