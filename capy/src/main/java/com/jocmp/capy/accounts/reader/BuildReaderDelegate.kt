package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.db.Database
import com.jocmp.readerclient.GoogleReader
import java.net.URI

internal fun buildReaderDelegate(
    source: Source,
    database: Database,
    path: URI,
    baseURL: String,
    password: String,
    clientCertAlias: String,
    clientCertManager: ClientCertManager,
): AccountDelegate {
    val httpClient = ReaderOkHttpClient.forAccount(path, password, clientCertAlias, clientCertManager)

    return ReaderAccountDelegate(
        source = source,
        database = database,
        googleReader = GoogleReader.create(
            client = httpClient,
            baseURL = baseURL
        )
    )
}
