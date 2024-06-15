package com.jocmp.capy.common

fun <T> MutableSet<T>.replace(element: T) {
    if (remove(element)) {
        add(element)
    }
}

fun <T> MutableSet<T>.upsert(element: T) {
    remove(element)
    add(element)
}
