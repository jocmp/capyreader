package com.capyreader.app.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.PreferenceStoreProvider
import com.jocmp.capy.preferences.AndroidPreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class DataStorePreferenceStoreProvider(
    private val context: Context
) : PreferenceStoreProvider {
    private val dataStores = ConcurrentHashMap<String, DataStore<Preferences>>()

    override fun build(accountID: String): AccountPreferences {
        val dataStore = dataStoreFor(accountID)
        return AccountPreferences(AndroidPreferenceStore(dataStore))
    }

    override fun delete(accountID: String) {
        val dataStore = dataStores.remove(accountID) ?: dataStoreFor(accountID)
        runBlocking {
            dataStore.edit { it.clear() }
        }
    }

    private fun dataStoreFor(accountID: String): DataStore<Preferences> {
        return dataStores.getOrPut(accountID) {
            val name = accountPrefs(accountID)
            PreferenceDataStoreFactory.create(
                migrations = listOf(SharedPreferencesMigration(context, name)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { context.preferencesDataStoreFile(name) }
            )
        }
    }
}

private fun accountPrefs(accountID: String) = "account_$accountID"
