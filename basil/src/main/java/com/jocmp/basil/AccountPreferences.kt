package com.jocmp.basil

import com.jocmp.basil.preferences.Preference
import com.jocmp.basil.preferences.PreferenceStore
import com.jocmp.basil.preferences.getEnum

class AccountPreferences(
    private val encryptedStore: PreferenceStore,
) {
    val username: Preference<String>
        get() = encryptedStore.getString("username", "")

    val password: Preference<String>
        get() = encryptedStore.getString("password", "")
}
