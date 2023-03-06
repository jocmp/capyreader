package com.jocmp.basilreader

import android.content.Context
import androidx.core.content.edit
import com.jocmp.basilreader.buildEncryptedPreferences

// For each account, create an account record
// ID, username (eventually `type`)

class CredentialsManager {
    companion object {
        const val accountID = "1337"
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"

        fun saveAccount(account: Account, context: Context) {
            val preferences = buildEncryptedPreferences(account.id, context)

            preferences.edit {
                putString(USERNAME_KEY, account.username)
                putString(PASSWORD_KEY, account.password)
            }
        }

        fun fetchAccount(context: Context): Account? {
            val preferences = buildEncryptedPreferences(accountID, context)

            val username = preferences.getString(USERNAME_KEY, "")
            val password = preferences.getString(PASSWORD_KEY, "")

            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                return null
            }

            return Account(
                id = accountID,
                username = username,
                password = password
            )
        }
    }
}
