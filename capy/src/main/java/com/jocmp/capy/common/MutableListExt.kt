package com.jocmp.capy.common

fun <T> MutableList<T>.replace(element: T) {
    val index = indexOf(element)

    if (index > -1) {
        set(index, element)
    }
}

fun <T> MutableList<T>.upsert(element: T) {
    val index = indexOf(element)

    if (index > -1) {
        set(index, element)
    } else {
        add(element)
    }
}
