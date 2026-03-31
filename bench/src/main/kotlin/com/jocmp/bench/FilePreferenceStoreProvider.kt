package com.jocmp.bench

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.PreferenceStoreProvider
import java.io.File

class FilePreferenceStoreProvider(private val prefsDir: File) : PreferenceStoreProvider {
    init {
        prefsDir.mkdirs()
    }

    override fun build(accountID: String): AccountPreferences {
        val file = File(prefsDir, "${accountID}.properties")
        return AccountPreferences(FilePreferenceStore(file))
    }

    override fun delete(accountID: String) {
        File(prefsDir, "${accountID}.properties").delete()
    }
}
