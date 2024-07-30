package com.jocmp.capy

import kotlinx.coroutines.runBlocking

internal fun <T> Int.repeated(action: (i: Int) -> T): List<T> {
    val list = mutableListOf<T>()

    repeat(this) {
        list.add(action(it))
    }

    return list
}

internal fun <T> Int.awaitRepeated(action: suspend (i: Int) -> T): List<T> {
    val list = mutableListOf<T>()

    repeat(this) {
        list.add(runBlocking { action(it) })
    }

    return list
}
