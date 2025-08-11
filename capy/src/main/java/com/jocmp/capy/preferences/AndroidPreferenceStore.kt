package com.jocmp.capy.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.jocmp.capy.preferences.AndroidPreference.BooleanPrimitive
import com.jocmp.capy.preferences.AndroidPreference.FloatPrimitive
import com.jocmp.capy.preferences.AndroidPreference.IntPrimitive
import com.jocmp.capy.preferences.AndroidPreference.LongPrimitive
import com.jocmp.capy.preferences.AndroidPreference.Object
import com.jocmp.capy.preferences.AndroidPreference.StringPrimitive
import com.jocmp.capy.preferences.AndroidPreference.StringSetPrimitive
import kotlinx.coroutines.runBlocking

class AndroidPreferenceStore(
    private val dataStore: DataStore<Preferences>,
) : PreferenceStore {

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return StringPrimitive(dataStore, key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return LongPrimitive(dataStore, key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return IntPrimitive(dataStore, key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return FloatPrimitive(dataStore, key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return BooleanPrimitive(dataStore, key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return StringSetPrimitive(dataStore, key, defaultValue)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T> {
        return Object(
            dataStore = dataStore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        )
    }

    override fun clearAll() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
}