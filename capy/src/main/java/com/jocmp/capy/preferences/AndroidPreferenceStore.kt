package com.jocmp.capy.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit

class AndroidPreferenceStore(
    private val dataStore: DataStore<Preferences>,
) : PreferenceStore {

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return AndroidPreference.StringPrimitive(dataStore, key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return AndroidPreference.LongPrimitive(dataStore, key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return AndroidPreference.IntPrimitive(dataStore, key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return AndroidPreference.FloatPrimitive(dataStore, key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return AndroidPreference.BooleanPrimitive(dataStore, key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return AndroidPreference.StringSetPrimitive(dataStore, key, defaultValue)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T> {
        return AndroidPreference.Object(
            dataStore = dataStore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        )
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
