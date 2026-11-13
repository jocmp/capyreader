package com.capyreader.app.preferences

import android.content.SharedPreferences
import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.PreferenceStore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class AndroidPreferenceStore(
    private val sharedPreferences: SharedPreferences,
) : PreferenceStore {
    private val keys = MutableSharedFlow<String?>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * One listener for the whole store. SharedPreferences only weakly references
     * listeners, so this field keeps it alive for the store's lifetime.
     */
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        keys.tryEmit(key)
    }

    private val keyFlow: Flow<String?> = keys

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return AndroidPreference.StringPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return AndroidPreference.LongPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return AndroidPreference.IntPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return AndroidPreference.FloatPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return AndroidPreference.BooleanPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return AndroidPreference.StringSetPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T> {
        return AndroidPreference.Object(
            preferences = sharedPreferences,
            keyFlow = keyFlow,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        )
    }

    override fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
