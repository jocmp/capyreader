package com.jocmp.basil

import com.jocmp.basil.preferences.Preference
import com.jocmp.basil.preferences.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class InMemoryPreferencesProvider() : PreferenceStoreProvider {
    override fun build(accountID: String): AccountPreferences {
        return AccountPreferences(preferenceStore = InMemoryDataStore())
    }

    override fun delete(accountID: String) {
    }
}

class InMemoryDataStore : PreferenceStore {
    private val store = mutableMapOf<String, Any>()

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
        deserializer: (String) -> T
    ): Preference<T> {
        return InMemoryPreference(key, defaultValue, store)
    }
}


class InMemoryPreference<T>(
    val key: String,
    val defaultValue: T,
    var store: MutableMap<String, Any>
) : Preference<T> {
    override fun key(): String {
        return key;
    }

    override fun get(): T {
        return store.getOrDefault(key, defaultValue) as T
    }

    override fun isSet(): Boolean {
        return store.contains(key)
    }

    override fun delete() {
        store.remove(key)
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun changes(): Flow<T> {
        TODO("Not yet implemented")
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        TODO("Not yet implemented")
    }

    override fun set(value: T) {
        store[key] = value as Any
    }

}
