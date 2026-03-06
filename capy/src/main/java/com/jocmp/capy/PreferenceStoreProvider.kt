package com.jocmp.capy

interface PreferenceStoreProvider {
    fun build(accountID: String): AccountPreferences

    suspend fun delete(accountID: String)
}
