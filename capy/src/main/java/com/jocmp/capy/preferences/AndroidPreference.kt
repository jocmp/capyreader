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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

sealed class AndroidPreference<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: String,
    private val defaultValue: T,
) : Preference<T> {

    abstract val preferenceKey: Preferences.Key<*>

    abstract fun read(preferences: Preferences): T

    abstract fun write(prefs: MutablePreferences, value: T)

    override fun key(): String = key

    override suspend fun get(): T = read(dataStore.data.first())

    override suspend fun set(value: T) {
        dataStore.edit { write(it, value) }
    }

    override suspend fun isSet(): Boolean = dataStore.data.first().contains(preferenceKey)

    override suspend fun delete() {
        dataStore.edit { it.remove(preferenceKey) }
    }

    override fun defaultValue(): T = defaultValue

    override fun changes(): Flow<T> {
        return dataStore.data
            .map { read(it) }
            .distinctUntilChanged()
    }

    class StringPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: String,
    ) : AndroidPreference<String>(dataStore, key, defaultValue) {
        override val preferenceKey = stringPreferencesKey(key)

        override fun read(preferences: Preferences): String {
            return preferences[preferenceKey] ?: defaultValue()
        }

        override fun write(prefs: MutablePreferences, value: String) {
            prefs[preferenceKey] = value
        }
    }

    class LongPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Long,
    ) : AndroidPreference<Long>(dataStore, key, defaultValue) {
        override val preferenceKey = longPreferencesKey(key)

        override fun read(preferences: Preferences): Long {
            return preferences[preferenceKey] ?: defaultValue()
        }

        override fun write(prefs: MutablePreferences, value: Long) {
            prefs[preferenceKey] = value
        }
    }

    class IntPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Int,
    ) : AndroidPreference<Int>(dataStore, key, defaultValue) {
        override val preferenceKey = intPreferencesKey(key)

        override fun read(preferences: Preferences): Int {
            return preferences[preferenceKey] ?: defaultValue()
        }

        override fun write(prefs: MutablePreferences, value: Int) {
            prefs[preferenceKey] = value
        }
    }

    class FloatPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Float,
    ) : AndroidPreference<Float>(dataStore, key, defaultValue) {
        override val preferenceKey = floatPreferencesKey(key)

        override fun read(preferences: Preferences): Float {
            return preferences[preferenceKey] ?: defaultValue()
        }

        override fun write(prefs: MutablePreferences, value: Float) {
            prefs[preferenceKey] = value
        }
    }

    class BooleanPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Boolean,
    ) : AndroidPreference<Boolean>(dataStore, key, defaultValue) {
        override val preferenceKey = booleanPreferencesKey(key)

        override fun read(preferences: Preferences): Boolean {
            return preferences[preferenceKey] ?: defaultValue()
        }

        override fun write(prefs: MutablePreferences, value: Boolean) {
            prefs[preferenceKey] = value
        }
    }

    class StringSetPrimitive(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: Set<String>,
    ) : AndroidPreference<Set<String>>(dataStore, key, defaultValue) {
        override val preferenceKey = stringSetPreferencesKey(key)

        override fun read(preferences: Preferences): Set<String> {
            return preferences[preferenceKey] ?: defaultValue()
        }

        override fun write(prefs: MutablePreferences, value: Set<String>) {
            prefs[preferenceKey] = value
        }
    }

    class Object<T>(
        dataStore: DataStore<Preferences>,
        key: String,
        defaultValue: T,
        private val serializer: (T) -> String,
        private val deserializer: (String) -> T,
    ) : AndroidPreference<T>(dataStore, key, defaultValue) {
        override val preferenceKey = stringPreferencesKey(key)

        override fun read(preferences: Preferences): T {
            return try {
                (preferences[preferenceKey] as? String)?.let(deserializer) ?: defaultValue()
            } catch (e: Exception) {
                defaultValue()
            }
        }

        override fun write(prefs: MutablePreferences, value: T) {
            prefs[preferenceKey] = serializer(value)
        }
    }
}
