package com.jocmp.basilreader.common

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.jocmp.basil.AccountPreferences
import com.jocmp.basil.PreferenceStoreProvider
import com.jocmp.basil.preferences.AndroidPreferenceStore

class AndroidPreferenceStoreProvider(
    private val context: Context
) : PreferenceStoreProvider {
    override fun build(accountID: String): AccountPreferences {
        return AccountPreferences(
            AndroidPreferenceStore(
                context.getSharedPreferences(
                    accountPrefs(accountID),
                    MODE_PRIVATE
                )
            )
        )
    }

    override fun delete(accountID: String) {
        context.getSharedPreferences(
            accountPrefs(accountID),
            MODE_PRIVATE
        ).edit {
            clear()
            commit()
        }
    }

    private fun accountPrefs(accountID: String) = "account_$accountID"
}
