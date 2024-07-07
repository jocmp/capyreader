package com.capyreader.common

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.PreferenceStoreProvider
import com.jocmp.capy.preferences.AndroidPreferenceStore


class SharedPreferenceStoreProvider(
    private val context: Context
) : PreferenceStoreProvider {
    override fun build(accountID: String): AccountPreferences {
        return AccountPreferences(
            AndroidPreferenceStore(
                buildPreferences(context, accountID)
            )
        )
    }

    override fun delete(accountID: String) {
        val preferences = buildPreferences(context, accountID)

        preferences.edit(commit = true) {
            clear()
        }
    }
}

private fun buildPreferences(context: Context, accountID: String) =
    context.getSharedPreferences(accountPrefs(accountID), MODE_PRIVATE)

private fun accountPrefs(accountID: String) = "account_$accountID"
