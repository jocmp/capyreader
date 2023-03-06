package com.jocmp.basilreader

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun buildEncryptedPreferences(accountID: String, context: Context): SharedPreferences {
    val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
    return EncryptedSharedPreferences.create(
        context,
        accountID,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
