package com.jocmp.capy

internal fun <T> Int.repeated(action: (i: Int) -> T): List<T> {
    val list = mutableListOf<T>()

    repeat(this) {
        list.add(action(it))
    }

    return list
}
