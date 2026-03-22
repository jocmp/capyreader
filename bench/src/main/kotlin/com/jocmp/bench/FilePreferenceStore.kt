package com.jocmp.bench

import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File
import java.util.Properties

class FilePreferenceStore(private val file: File) : PreferenceStore {
    private val props = Properties()
    private val keyFlow = MutableStateFlow<String?>(null)

    init {
        if (file.exists()) {
            file.inputStream().use { props.load(it) }
        }
    }

    private fun flush() {
        file.parentFile?.mkdirs()
        file.outputStream().use { props.store(it, null) }
    }

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return FilePref(key, defaultValue, keyFlow,
            get = { props.getProperty(key, defaultValue) },
            set = { props.setProperty(key, it); flush() },
            del = { props.remove(key); flush() },
            has = { props.containsKey(key) },
        )
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return FilePref(key, defaultValue, keyFlow,
            get = { props.getProperty(key)?.toLongOrNull() ?: defaultValue },
            set = { props.setProperty(key, it.toString()); flush() },
            del = { props.remove(key); flush() },
            has = { props.containsKey(key) },
        )
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return FilePref(key, defaultValue, keyFlow,
            get = { props.getProperty(key)?.toIntOrNull() ?: defaultValue },
            set = { props.setProperty(key, it.toString()); flush() },
            del = { props.remove(key); flush() },
            has = { props.containsKey(key) },
        )
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return FilePref(key, defaultValue, keyFlow,
            get = { props.getProperty(key)?.toFloatOrNull() ?: defaultValue },
            set = { props.setProperty(key, it.toString()); flush() },
            del = { props.remove(key); flush() },
            has = { props.containsKey(key) },
        )
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return FilePref(key, defaultValue, keyFlow,
            get = { props.getProperty(key)?.toBooleanStrictOrNull() ?: defaultValue },
            set = { props.setProperty(key, it.toString()); flush() },
            del = { props.remove(key); flush() },
            has = { props.containsKey(key) },
        )
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return FilePref(key, defaultValue, keyFlow,
            get = {
                val raw = props.getProperty(key) ?: return@FilePref defaultValue
                if (raw.isEmpty()) emptySet() else raw.split("\u001F").toSet()
            },
            set = { props.setProperty(key, it.joinToString("\u001F")); flush() },
            del = { props.remove(key); flush() },
            has = { props.containsKey(key) },
        )
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T> {
        return FilePref(key, defaultValue, keyFlow,
            get = {
                val raw = props.getProperty(key) ?: return@FilePref defaultValue
                try { deserializer(raw) } catch (_: Exception) { defaultValue }
            },
            set = { props.setProperty(key, serializer(it)); flush() },
            del = { props.remove(key); flush() },
            has = { props.containsKey(key) },
        )
    }

    override fun clearAll() {
        props.clear()
        flush()
    }
}

private class FilePref<T>(
    private val key: String,
    private val default: T,
    private val keyFlow: MutableStateFlow<String?>,
    private val get: () -> T,
    private val set: (T) -> Unit,
    private val del: () -> Unit,
    private val has: () -> Boolean,
) : Preference<T> {
    override fun key() = key
    override fun get() = get.invoke()
    override fun set(value: T) { set.invoke(value); keyFlow.value = key }
    override fun isSet() = has()
    override fun delete() { del(); keyFlow.value = key }
    override fun defaultValue() = default

    override fun changes(): Flow<T> {
        return keyFlow.map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return changes().stateIn(scope, SharingStarted.Eagerly, get())
    }
}
