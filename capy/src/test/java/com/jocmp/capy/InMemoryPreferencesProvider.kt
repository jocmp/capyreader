package com.jocmp.capy

import com.jocmp.capy.preferences.InMemoryPreferenceStore

class InMemoryPreferencesProvider : PreferenceStoreProvider {
    override fun build(accountID: String): AccountPreferences {
        return AccountPreferences(store = InMemoryPreferenceStore())
    }

    override fun delete(accountID: String) {
    }
}
