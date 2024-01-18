package com.jocmp.basil

import com.jocmp.basil.preferences.Preference
import com.jocmp.basil.preferences.PreferenceStore
import com.jocmp.basil.preferences.getEnum
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class AccountPreferences(private val preferenceStore: PreferenceStore) {
    val source: AccountSource
        get() = preferenceStore.getEnum("source", AccountSource.LOCAL).get()
}
