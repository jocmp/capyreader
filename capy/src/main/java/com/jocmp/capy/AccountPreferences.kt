package com.jocmp.capy

import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.PreferenceStore

class AccountPreferences(
    private val encryptedStore: PreferenceStore,
) {
    val username: Preference<String>
        get() = encryptedStore.getString("username", "")

    val password: Preference<String>
        get() = encryptedStore.getString("password", "")
}
