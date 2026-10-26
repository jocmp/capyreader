package com.jocmp.mercury.utils

fun range(
    start: Int = 1,
    end: Int = 1,
): Sequence<Int> =
    sequence {
        var i = start
        while (i <= end) {
            yield(i)
            i++
        }
    }
