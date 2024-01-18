package com.jocmp.basil

import com.jocmp.basil.preferences.PreferenceStore

interface PreferenceStoreProvider {
    fun build(accountID: String): PreferenceStore

    fun delete(accountID: String)
}
