package com.jocmp.basilreader.di

import com.jocmp.basilreader.lib.PreferencesCredentialsManager
import com.jocmp.feedbinclient.CredentialsManager
import org.koin.dsl.module

internal val credentialsModule = module {
    single<CredentialsManager> { PreferencesCredentialsManager(get()) }
}
