package com.jocmp.basil

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InMemoryPreferencesProvider(preferences: AccountPreferences) : PreferencesProvider {
    private val dataStore = InMemoryDataStore(preferences)

    override fun forAccount(accountID: String): DataStore<AccountPreferences> {
        return dataStore
    }
}

class InMemoryDataStore(
    private var preferences: AccountPreferences
) : DataStore<AccountPreferences> {
    override val data: Flow<AccountPreferences> =
        flow {
            emit(preferences)
        }

    override suspend fun updateData(transform: suspend (t: AccountPreferences) -> AccountPreferences): AccountPreferences {
        return transform(preferences)
    }
}
