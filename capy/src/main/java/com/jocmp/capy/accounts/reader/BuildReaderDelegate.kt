package com.jocmp.capy.accounts.reader

import android.content.Context
import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.db.Database
import com.jocmp.readerclient.GoogleReader
import java.net.URI

internal fun buildReaderDelegate(
    context: Context,
    source: Source,
    database: Database,
    path: URI,
    preferences: AccountPreferences
): AccountDelegate {
    val httpClient = ReaderOkHttpClient.forAccount(context, path, preferences)

    return ReaderAccountDelegate(
        source = source,
        database = database,
        googleReader = GoogleReader.create(
            client = httpClient,
            baseURL = preferences.url.get()
        )
    )
}
