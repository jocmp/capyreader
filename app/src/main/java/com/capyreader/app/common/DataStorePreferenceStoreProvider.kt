package com.capyreader.app.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.PreferenceStoreProvider
import com.jocmp.capy.preferences.DataStorePreferenceStore
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DataStorePreferenceStoreProvider(
    private val context: Context,
) : PreferenceStoreProvider {
    private val instances = ConcurrentHashMap<String, DataStore<Preferences>>()

    override fun build(accountID: String): AccountPreferences {
        val dataStore = instances.getOrPut(accountID) {
            createDataStore(accountID)
        }

        return AccountPreferences(DataStorePreferenceStore(dataStore))
    }

    override suspend fun delete(accountID: String) {
        val dataStore = instances.remove(accountID)

        dataStore?.edit { it.clear() }

        dataStoreFile(accountID).delete()
    }

    private fun createDataStore(accountID: String): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            migrations = listOf(
                SharedPreferencesMigration(context, accountPrefsName(accountID))
            ),
            produceFile = { dataStoreFile(accountID) }
        )
    }

    private fun dataStoreFile(accountID: String): File {
        return File(context.filesDir, "datastore/account_$accountID.preferences_pb")
    }
}

private fun accountPrefsName(accountID: String) = "account_$accountID"
