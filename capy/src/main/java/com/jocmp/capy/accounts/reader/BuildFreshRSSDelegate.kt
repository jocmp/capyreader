package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.db.Database
import com.jocmp.readerclient.GoogleReader
import java.net.URI

internal fun buildReaderDelegate(
    database: Database,
    path: URI,
    preferences: AccountPreferences
): AccountDelegate {
    val httpClient = ReaderOkHttpClient.forAccount(path, preferences)

    return ReaderAccountDelegate(
        database = database,
        googleReader = GoogleReader.create(
            client = httpClient,
            baseURL = preferences.url.get()
        )
    )
}
