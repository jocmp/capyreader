package com.jocmp.capy

import okhttp3.OkHttpClient

fun interface ClientCertManager {
    fun configure(builder: OkHttpClient.Builder, certAlias: String): OkHttpClient.Builder
}
