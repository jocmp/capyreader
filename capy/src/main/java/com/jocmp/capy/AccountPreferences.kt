package com.jocmp.capy

import com.jocmp.capy.accounts.Source
import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.PreferenceStore
import com.jocmp.capy.preferences.getEnum

class AccountPreferences(
    private val encryptedStore: PreferenceStore,
) {
    val source: Preference<Source>
        get() = encryptedStore.getEnum("source", Source.LOCAL)

    val username: Preference<String>
        get() = encryptedStore.getString("username", "")

    val password: Preference<String>
        get() = encryptedStore.getString("password", "")
}
