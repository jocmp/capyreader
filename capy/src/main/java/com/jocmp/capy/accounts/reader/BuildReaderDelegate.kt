package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.db.Database
import com.jocmp.readerclient.GoogleReader
import java.net.URI

internal fun buildReaderDelegate(
    source: Source,
    database: Database,
    path: URI,
    preferences: AccountPreferences,
    clientCertManager: ClientCertManager,
): AccountDelegate {
    val httpClient = ReaderOkHttpClient.forAccount(path, preferences, clientCertManager)

    return ReaderAccountDelegate(
        source = source,
        database = database,
        googleReader = GoogleReader.create(
            client = httpClient,
            baseURL = preferences.url.get()
        )
    )
}
