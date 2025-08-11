package com.jocmp.capy.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

sealed class AndroidPreference<T>(
    protected val dataStore: DataStore<Preferences>,
    protected val key: String,
    private val defaultValue: T,
) : Preference<T> {

    abstract fun read(preferences: Preferences, key: String, defaultValue: T): T

    abstract suspend fun write(value: T): Preferences.() -> Preferences

    abstract fun getPreferenceKey(key: String): Preferences.Key<*>

    override fun key(): String {
        return key
    }

    override fun get(): T {
        return runBlocking {
            dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { preferences -> read(preferences, key, defaultValue) }
                .first()
        }
    }

    override suspend fun set(value: T) {
        dataStore.edit { preferences ->
            write(value)(preferences)
        }
    }

    override suspend fun delete() {
        dataStore.edit { preferences ->
            preferences.toMutablePreferences().apply {
                remove(getPreferenceKey(key))
            }
        }
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun changes(): Flow<T> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> read(preferences, key, defaultValue) }
            .onStart { emit(get()) }
            .conflate()
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return changes().stateIn(scope, SharingStarted.Eagerly, get())
    }

    class StringPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: String,
    ) : AndroidPreference<String>(dataStore, key, defaultValue) {
        override fun read(preferences: Preferences, key: String, defaultValue: String): String {
            return preferences[stringPreferencesKey(key)] ?: defaultValue
        }

        override suspend fun write(value: String): Preferences.() -> Preferences = {
            toMutablePreferences().apply {
                set(stringPreferencesKey(key), value)
            }
        }

        override fun getPreferenceKey(key: String): Preferences.Key<String> {
            return stringPreferencesKey(key)
        }
    }

    class LongPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Long,
    ) : AndroidPreference<Long>(dataStore, key, defaultValue) {
        override fun read(preferences: Preferences, key: String, defaultValue: Long): Long {
            return preferences[longPreferencesKey(key)] ?: defaultValue
        }

        override suspend fun write(value: Long): Preferences.() -> Preferences = {
            toMutablePreferences().apply {
                set(longPreferencesKey(key), value)
            }
        }

        override fun getPreferenceKey(key: String): Preferences.Key<Long> {
            return longPreferencesKey(key)
        }
    }

    class IntPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Int,
    ) : AndroidPreference<Int>(dataStore, key, defaultValue) {
        override fun read(preferences: Preferences, key: String, defaultValue: Int): Int {
            return preferences[intPreferencesKey(key)] ?: defaultValue
        }

        override suspend fun write(value: Int): Preferences.() -> Preferences = {
            toMutablePreferences().apply {
                set(intPreferencesKey(key), value)
            }
        }

        override fun getPreferenceKey(key: String): Preferences.Key<Int> {
            return intPreferencesKey(key)
        }
    }

    class FloatPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Float,
    ) : AndroidPreference<Float>(dataStore, key, defaultValue) {
        override fun read(preferences: Preferences, key: String, defaultValue: Float): Float {
            return preferences[floatPreferencesKey(key)] ?: defaultValue
        }

        override suspend fun write(value: Float): Preferences.() -> Preferences = {
            toMutablePreferences().apply {
                set(floatPreferencesKey(key), value)
            }
        }

        override fun getPreferenceKey(key: String): Preferences.Key<Float> {
            return floatPreferencesKey(key)
        }
    }

    class BooleanPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Boolean,
    ) : AndroidPreference<Boolean>(dataStore, key, defaultValue) {
        override fun read(preferences: Preferences, key: String, defaultValue: Boolean): Boolean {
            return preferences[booleanPreferencesKey(key)] ?: defaultValue
        }

        override suspend fun write(value: Boolean): Preferences.() -> Preferences = {
            toMutablePreferences().apply {
                set(booleanPreferencesKey(key), value)
            }
        }

        override fun getPreferenceKey(key: String): Preferences.Key<Boolean> {
            return booleanPreferencesKey(key)
        }
    }

    class StringSetPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Set<String>,
    ) : AndroidPreference<Set<String>>(dataStore, key, defaultValue) {
        override fun read(
            preferences: Preferences,
            key: String,
            defaultValue: Set<String>
        ): Set<String> {
            return preferences[stringSetPreferencesKey(key)] ?: defaultValue
        }

        override suspend fun write(value: Set<String>): Preferences.() -> Preferences = {
            toMutablePreferences().apply {
                set(stringSetPreferencesKey(key), value)
            }
        }

        override fun getPreferenceKey(key: String): Preferences.Key<Set<String>> {
            return stringSetPreferencesKey(key)
        }
    }

    class Object<T>(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: T,
        val serializer: (T) -> String,
        val deserializer: (String) -> T,
    ) : AndroidPreference<T>(dataStore, key, defaultValue) {
        override fun read(preferences: Preferences, key: String, defaultValue: T): T {
            return try {
                preferences[stringPreferencesKey(key)]?.let(deserializer) ?: defaultValue
            } catch (e: Exception) {
                defaultValue
            }
        }

        override suspend fun write(value: T): Preferences.() -> Preferences = {
            toMutablePreferences().apply {
                set(stringPreferencesKey(key), serializer(value))
            }
        }

        override fun getPreferenceKey(key: String): Preferences.Key<String> {
            return stringPreferencesKey(key)
        }
    }
}