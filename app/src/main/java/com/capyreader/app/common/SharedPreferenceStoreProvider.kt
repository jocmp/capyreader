package com.capyreader.app.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.PreferenceStoreProvider
import com.jocmp.capy.preferences.AndroidPreferenceStore
import java.io.File

class SharedPreferenceStoreProvider(
    private val context: Context
) : PreferenceStoreProvider {
    override fun build(accountID: String): AccountPreferences {
        return AccountPreferences(
            AndroidPreferenceStore(
                buildDataStore(context, accountID)
            )
        )
    }

    override suspend fun delete(accountID: String) {
        val dataStore = buildDataStore(context, accountID)

        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

private fun buildDataStore(context: Context, accountID: String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        produceFile = {
            File(context.filesDir, "datastore/${accountPrefs(accountID)}.preferences_pb")
        }
    )
}

private fun accountPrefs(accountID: String) = "account_$accountID"