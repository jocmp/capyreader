package com.jocmp.bench

import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.FaviconPolicy
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.common.withTrailingSeparator
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Properties

data class BenchConfig(
    val source: Source,
    val username: String,
    val password: String,
    val url: String,
)

fun loadConfig(benchDir: File): BenchConfig {
    val propsFile = File(benchDir, "bench.properties")
    require(propsFile.exists()) {
        "Missing bench/bench.properties. Copy bench.properties.example and fill in your credentials."
    }

    val props = Properties()
    propsFile.inputStream().use { props.load(it) }

    val sourceName = props.getProperty("source") ?: error("Missing 'source' in bench.properties")

    return BenchConfig(
        source = Source.entries.first { it.value == sourceName },
        username = props.getProperty("username").orEmpty(),
        password = props.getProperty("password").orEmpty(),
        url = props.getProperty("url").orEmpty().withTrailingSeparator,
    )
}

fun loadOrCreateAccount(benchDir: File, config: BenchConfig): Pair<AccountManager, Account> {
    val dataDir = File(benchDir, "data")
    val accountsDir = File(dataDir, "accounts").apply { mkdirs() }
    val cacheDir = File(dataDir, "cache").apply { mkdirs() }
    val dbDir = File(dataDir, "db")
    val prefsDir = File(dataDir, "prefs")

    val databaseProvider = FileDatabaseProvider(dbDir)
    val preferenceStoreProvider = FilePreferenceStoreProvider(prefsDir)

    val manager = AccountManager(
        rootFolder = accountsDir.toURI(),
        cacheDirectory = cacheDir.toURI(),
        databaseProvider = databaseProvider,
        preferenceStoreProvider = preferenceStoreProvider,
        faviconPolicy = FaviconPolicy { true },
        userAgent = "CapyBench/1.0",
        acceptLanguage = "en-US",
    )

    val accountIDFile = File(dataDir, "account_id")

    val account = if (accountIDFile.exists()) {
        val id = accountIDFile.readText().trim()
        manager.findByID(id) ?: error("Account $id not found. Run 'reset' and try again.")
    } else {
        println("Verifying credentials...")
        val verified = runBlocking {
            Credentials.from(
                source = config.source,
                username = config.username,
                password = config.password,
                url = config.url,
            ).verify().getOrThrow()
        }

        println("Authenticated as ${verified.username}")

        val id = manager.createAccount(
            username = verified.username,
            password = verified.secret,
            url = verified.url,
            source = verified.source,
        )
        accountIDFile.writeText(id)
        manager.findByID(id)!!
    }

    return manager to account
}
