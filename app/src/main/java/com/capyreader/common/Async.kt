package com.capyreader.common

sealed class Async<out T>(private val value: T?) {
    open operator fun invoke(): T? = value

    data object Uninitialized : Async<Nothing>(value = null)

    data object Loading : Async<Nothing>(value = null)

    data class Success<out T>(private val value: T) : Async<T>(value = value) {
        override operator fun invoke(): T = value
    }

    data class Failure<out T>(val error: Throwable, private val value: T? = null) : Async<T>(value = value)
}
