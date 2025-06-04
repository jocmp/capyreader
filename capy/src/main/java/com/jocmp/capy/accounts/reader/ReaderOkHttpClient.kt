package com.jocmp.capy.accounts.reader

import android.content.Context
import android.security.KeyChain
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.accounts.BasicAuthInterceptor
import com.jocmp.capy.accounts.httpClientBuilder
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import java.net.Socket
import java.net.URI
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509KeyManager

internal object ReaderOkHttpClient {
    fun forAccount(context: Context, path: URI, preferences: AccountPreferences): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(
                BasicAuthInterceptor {
                    val secret = preferences.password.get()

                    "GoogleLogin auth=${secret}"
                }
            )
            .clientCertAlias(context, preferences.clientCertAlias.get())
            .build()
    }

    fun OkHttpClient.Builder.clientCertAlias(context: Context, clientCertAlias: String): OkHttpClient.Builder {
        if (clientCertAlias.isNotEmpty()) {
            val clientKeyManager = object : X509KeyManager {
                override fun getClientAliases(keyType: String?, issuers: Array<Principal>?) =
                    throw UnsupportedOperationException("getClientAliases")

                override fun chooseClientAlias(
                    keyType: Array<String>?,
                    issuers: Array<Principal>?,
                    socket: Socket?
                ) = clientCertAlias

                override fun getServerAliases(keyType: String?, issuers: Array<Principal>?) =
                    throw UnsupportedOperationException("getServerAliases")

                override fun chooseServerAlias(
                    keyType: String?,
                    issuers: Array<Principal>?,
                    socket: Socket?
                ) = throw UnsupportedOperationException("chooseServerAlias")

                override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
                    return if (alias == clientCertAlias) KeyChain.getCertificateChain(context, clientCertAlias) else null
                }

                override fun getPrivateKey(alias: String?): PrivateKey? {
                    return if (alias == clientCertAlias) KeyChain.getPrivateKey(context, clientCertAlias) else null
                }
            }

            val sslContext = SSLContext.getInstance("TLS")
            val trustManager = Platform.get().platformTrustManager()
            sslContext.init(arrayOf(clientKeyManager), arrayOf(trustManager), null)
            val sslSocketFactory = sslContext.socketFactory

            sslSocketFactory(sslSocketFactory, trustManager)
        }
        return this
    }
}
