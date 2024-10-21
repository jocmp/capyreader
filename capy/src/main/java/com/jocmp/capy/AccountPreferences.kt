package com.jocmp.capy

import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.PreferenceStore
import com.jocmp.capy.preferences.getEnum

class AccountPreferences(
    private val store: PreferenceStore,
) {
    val source: Preference<Source>
        get() = store.getEnum("source", Source.LOCAL)

    val username: Preference<String>
        get() = store.getString("username", "")

    val url: Preference<String>
        get() = store.getString("api_url", "")

    val password: Preference<String>
        get() = store.getString("password", "")

    val autoDelete: Preference<AutoDelete>
        get() = store.getEnum("auto_delete_articles", AutoDelete.default)
}
