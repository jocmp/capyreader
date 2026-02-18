package com.jocmp.capy.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryPreferenceStore : PreferenceStore {
    private val store = MutableStateFlow(mapOf<String, Any>())

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return InMemoryPreference(key, defaultValue, store)
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return InMemoryPreference(key, defaultValue, store)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return InMemoryPreference(key, defaultValue, store)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return InMemoryPreference(key, defaultValue, store)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return InMemoryPreference(key, defaultValue, store)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return InMemoryPreference(key, defaultValue, store)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T> {
        return InMemoryPreference(key, defaultValue, store)
    }

    override suspend fun clearAll() {
        store.value = emptyMap()
    }
}

private class InMemoryPreference<T>(
    private val key: String,
    private val defaultValue: T,
    private val store: MutableStateFlow<Map<String, Any>>,
) : Preference<T> {
    override fun key(): String = key

    @Suppress("UNCHECKED_CAST")
    override suspend fun get(): T = store.value.getOrDefault(key, defaultValue) as T

    override suspend fun set(value: T) {
        store.update { it + (key to (value as Any)) }
    }

    override suspend fun isSet(): Boolean = store.value.containsKey(key)

    override suspend fun delete() {
        store.update { it - key }
    }

    override fun defaultValue(): T = defaultValue

    @Suppress("UNCHECKED_CAST")
    override fun changes(): Flow<T> {
        return store.map { it.getOrDefault(key, defaultValue) as T }
    }
}
