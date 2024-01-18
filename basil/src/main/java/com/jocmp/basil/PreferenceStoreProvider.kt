package com.jocmp.basil

interface PreferenceStoreProvider {
    fun build(accountID: String): AccountPreferences

    fun delete(accountID: String)
}
