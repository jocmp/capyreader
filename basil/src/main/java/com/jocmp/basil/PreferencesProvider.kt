package com.jocmp.basil

import androidx.datastore.core.DataStore

interface PreferencesProvider {
    fun forAccount(accountID: String): DataStore<AccountPreferences>
}
