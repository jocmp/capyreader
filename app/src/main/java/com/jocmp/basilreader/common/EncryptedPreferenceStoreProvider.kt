package com.jocmp.basilreader.common

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.jocmp.basil.AccountPreferences
import com.jocmp.basil.PreferenceStoreProvider
import com.jocmp.basil.preferences.AndroidPreferenceStore


class EncryptedPreferenceStoreProvider(
    private val context: Context
) : PreferenceStoreProvider {
    override fun build(accountID: String): AccountPreferences {
        return AccountPreferences(
            AndroidPreferenceStore(
                buildEncryptedPreferences(context, accountPrefs(accountID))
            )
        )
    }

    override fun delete(accountID: String) {
        val preferences = buildEncryptedPreferences(context, accountPrefs(accountID))

        preferences.edit {
            clear()
            commit()
        }
    }

    private fun accountPrefs(accountID: String) = "account_$accountID"
}


// https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
fun buildEncryptedPreferences(context: Context, fileName: String): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        fileName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
