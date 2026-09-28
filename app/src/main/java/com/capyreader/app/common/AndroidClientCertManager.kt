package com.capyreader.app.common

import android.content.Context
import android.security.KeyChain
import com.jocmp.capy.ClientCertManager
import okhttp3.OkHttpClient
import java.net.Socket
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509KeyManager
import javax.net.ssl.X509TrustManager

class AndroidClientCertManager(private val context: Context) : ClientCertManager {
    override fun configure(builder: OkHttpClient.Builder, certAlias: String): OkHttpClient.Builder {
        val clientKeyManager = object : X509KeyManager {
            override fun getClientAliases(keyType: String?, issuers: Array<Principal>?) =
                throw UnsupportedOperationException("getClientAliases")

            override fun chooseClientAlias(
                keyType: Array<String>?,
                issuers: Array<Principal>?,
                socket: Socket?
            ) = certAlias

            override fun getServerAliases(keyType: String?, issuers: Array<Principal>?) =
                throw UnsupportedOperationException("getServerAliases")

            override fun chooseServerAlias(
                keyType: String?,
                issuers: Array<Principal>?,
                socket: Socket?
            ) = throw UnsupportedOperationException("chooseServerAlias")

            override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
                return if (alias == certAlias) KeyChain.getCertificateChain(context, certAlias) else null
            }

            override fun getPrivateKey(alias: String?): PrivateKey? {
                return if (alias == certAlias) KeyChain.getPrivateKey(context, certAlias) else null
            }
        }

        val sslContext = SSLContext.getInstance("TLS")
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManager = trustManagerFactory.trustManagers.filterIsInstance<X509TrustManager>().first()
        sslContext.init(arrayOf(clientKeyManager), arrayOf(trustManager), null)

        return builder.sslSocketFactory(sslContext.socketFactory, trustManager)
    }
}
