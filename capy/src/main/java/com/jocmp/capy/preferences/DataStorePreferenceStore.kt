package com.jocmp.capy.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.runBlocking

class DataStorePreferenceStore(
    private val dataStore: DataStore<Preferences>,
) : PreferenceStore {

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return DataStorePreference.StringPrimitive(dataStore, key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return DataStorePreference.LongPrimitive(dataStore, key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return DataStorePreference.IntPrimitive(dataStore, key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return DataStorePreference.FloatPrimitive(dataStore, key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return DataStorePreference.BooleanPrimitive(dataStore, key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return DataStorePreference.StringSetPrimitive(dataStore, key, defaultValue)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T> {
        return DataStorePreference.Object(
            dataStore = dataStore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        )
    }

    override fun clearAll() {
        runBlocking {
            dataStore.edit { it.clear() }
        }
    }
}
