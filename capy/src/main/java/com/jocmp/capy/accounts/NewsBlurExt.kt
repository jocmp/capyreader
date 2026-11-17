package com.jocmp.capy.accounts

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.newsblur.NewsBlurOkHttpClient
import com.jocmp.newsblurclient.NewsBlur
import java.net.URI

fun NewsBlur.Companion.forAccount(
    path: URI,
    preferences: AccountPreferences,
    clientCertManager: ClientCertManager = ClientCertManager { builder, _ -> builder },
) = create(
    client = NewsBlurOkHttpClient.forAccount(path, preferences, clientCertManager),
    baseURL = preferences.url.get().ifBlank { NewsBlur.DEFAULT_URL },
)
