package com.jocmp.capy.preferences

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Preference<T> {

    fun key(): String

    fun get(): T

    suspend  fun set(value: T)

    suspend fun delete()

    fun defaultValue(): T

    fun changes(): Flow<T>

    fun stateIn(scope: CoroutineScope): StateFlow<T>
}

suspend inline fun <reified T, R : T> Preference<T>.getAndSet(crossinline block: (T) -> R) = set(block(get()))
