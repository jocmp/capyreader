package com.capyreader.lite.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import com.jocmp.capy.preferences.PreferenceStore

class LitePreferences(context: Context) {
    private val preferenceStore: PreferenceStore = AndroidPreferenceStore(
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    )

    val accountID = preferenceStore.getString("account_id", "")

    val isLoggedIn: Boolean
        get() = accountID.get().isNotBlank()
}
