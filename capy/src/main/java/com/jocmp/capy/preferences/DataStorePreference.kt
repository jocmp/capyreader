package com.jocmp.capy.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

sealed class DataStorePreference<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: String,
    private val defaultValue: T,
) : Preference<T> {

    abstract val prefKey: Preferences.Key<*>

    abstract fun read(prefs: Preferences): T

    abstract suspend fun write(prefs: MutablePreferences, value: T)

    override fun key(): String = key

    override suspend fun get(): T =
        dataStore.data.map { read(it) }.first()

    override suspend fun set(value: T) {
        dataStore.edit { write(it, value) }
    }

    override suspend fun isSet(): Boolean =
        dataStore.data.map { it.contains(prefKey) }.first()

    override suspend fun delete() {
        dataStore.edit { it.remove(prefKey) }
    }

    override fun defaultValue(): T = defaultValue

    override fun changes(): Flow<T> {
        return dataStore.data
            .map { read(it) }
            .distinctUntilChanged()
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return changes().stateIn(scope, SharingStarted.Eagerly, runBlocking { get() })
    }

    class StringPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: String,
    ) : DataStorePreference<String>(dataStore, key, defaultValue) {
        override val prefKey = stringPreferencesKey(key)

        override fun read(prefs: Preferences): String = prefs[prefKey] ?: defaultValue()

        override suspend fun write(prefs: MutablePreferences, value: String) {
            prefs[prefKey] = value
        }
    }

    class LongPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Long,
    ) : DataStorePreference<Long>(dataStore, key, defaultValue) {
        override val prefKey = longPreferencesKey(key)

        override fun read(prefs: Preferences): Long = prefs[prefKey] ?: defaultValue()

        override suspend fun write(prefs: MutablePreferences, value: Long) {
            prefs[prefKey] = value
        }
    }

    class IntPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Int,
    ) : DataStorePreference<Int>(dataStore, key, defaultValue) {
        override val prefKey = intPreferencesKey(key)

        override fun read(prefs: Preferences): Int = prefs[prefKey] ?: defaultValue()

        override suspend fun write(prefs: MutablePreferences, value: Int) {
            prefs[prefKey] = value
        }
    }

    class FloatPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Float,
    ) : DataStorePreference<Float>(dataStore, key, defaultValue) {
        override val prefKey = floatPreferencesKey(key)

        override fun read(prefs: Preferences): Float = prefs[prefKey] ?: defaultValue()

        override suspend fun write(prefs: MutablePreferences, value: Float) {
            prefs[prefKey] = value
        }
    }

    class BooleanPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Boolean,
    ) : DataStorePreference<Boolean>(dataStore, key, defaultValue) {
        override val prefKey = booleanPreferencesKey(key)

        override fun read(prefs: Preferences): Boolean = prefs[prefKey] ?: defaultValue()

        override suspend fun write(prefs: MutablePreferences, value: Boolean) {
            prefs[prefKey] = value
        }
    }

    class StringSetPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Set<String>,
    ) : DataStorePreference<Set<String>>(dataStore, key, defaultValue) {
        override val prefKey = stringSetPreferencesKey(key)

        override fun read(prefs: Preferences): Set<String> = prefs[prefKey] ?: defaultValue()

        override suspend fun write(prefs: MutablePreferences, value: Set<String>) {
            prefs[prefKey] = value
        }
    }

    class Object<T>(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: T,
        private val serializer: (T) -> String,
        private val deserializer: (String) -> T,
    ) : DataStorePreference<T>(dataStore, key, defaultValue) {
        override val prefKey = stringPreferencesKey(key)

        override fun read(prefs: Preferences): T {
            return try {
                prefs[prefKey]?.let(deserializer) ?: defaultValue()
            } catch (e: Exception) {
                defaultValue()
            }
        }

        override suspend fun write(prefs: MutablePreferences, value: T) {
            prefs[prefKey] = serializer(value)
        }
    }
}
