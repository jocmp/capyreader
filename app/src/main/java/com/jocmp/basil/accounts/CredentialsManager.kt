package com.jocmp.basil.accounts

import android.content.Context
import androidx.core.content.edit

class CredentialsManager {
    companion object {
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"

        fun saveAccount(account: Account, context: Context) {
            val preferences = buildEncryptedPreferences(account.username, context)

            preferences.edit {
                putString(USERNAME_KEY, account.username)
                putString(PASSWORD_KEY, account.password)
            }
        }

        fun fetchAccount(context: Context): Account? {
            val preferences = buildEncryptedPreferences("jocmp64@gmail.com", context)

            val username = preferences.getString(USERNAME_KEY, "")
            val password = preferences.getString(PASSWORD_KEY, "")

            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                return null
            }

            return Account(
                username = username,
                password = password
            )
        }
    }
}
