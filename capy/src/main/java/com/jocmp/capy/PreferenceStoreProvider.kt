package com.jocmp.capy

interface PreferenceStoreProvider {
    fun build(accountID: String): AccountPreferences

    fun delete(accountID: String)
}
