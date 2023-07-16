package com.jocmp.basilreader.lib

import android.content.Context
import androidx.core.content.edit
import com.jocmp.feedbinclient.Account
import com.jocmp.feedbinclient.CredentialsManager

class PreferencesCredentialsManager(private val context: Context): CredentialsManager {
    override fun save(account: Account) {
        val preferences = buildEncryptedPreferences(context)

        preferences.edit {
            putString(USERNAME_KEY, account.username)
            putString(PASSWORD_KEY, account.password)
        }
    }

    override fun fetch(): Account {
        val preferences = buildEncryptedPreferences(context)

        val username = preferences.getString(USERNAME_KEY, "").toString()
        val password = preferences.getString(PASSWORD_KEY, "").toString()

        return Account(
            username = username,
            password = password
        )
    }

    override val hasAccount: Boolean
        get() {
            val preferences = buildEncryptedPreferences(context)
            val username = preferences.getString(USERNAME_KEY, "").toString()
            val password = preferences.getString(PASSWORD_KEY, "").toString()

            return username.isNotBlank() && password.isNotBlank()
        }

    companion object {
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"
    }
}