package com.jocmp.capy.preferences

import android.content.SharedPreferences
import com.jocmp.capy.preferences.AndroidPreference.BooleanPrimitive
import com.jocmp.capy.preferences.AndroidPreference.FloatPrimitive
import com.jocmp.capy.preferences.AndroidPreference.IntPrimitive
import com.jocmp.capy.preferences.AndroidPreference.LongPrimitive
import com.jocmp.capy.preferences.AndroidPreference.Object
import com.jocmp.capy.preferences.AndroidPreference.StringPrimitive
import com.jocmp.capy.preferences.AndroidPreference.StringSetPrimitive
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class AndroidPreferenceStore(
    private val sharedPreferences: SharedPreferences,
) : PreferenceStore {
    private val keyFlow = sharedPreferences.keyFlow

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return StringPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return LongPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return IntPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return FloatPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return BooleanPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return StringSetPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T> {
        return Object(
            preferences = sharedPreferences,
            keyFlow = keyFlow,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        )
    }
}

private val SharedPreferences.keyFlow
    get() = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? -> trySend(key) }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
