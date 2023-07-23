package com.jocmp.basilreader.di
import com.jocmp.feedbinclient.Authentication
import com.jocmp.feedbinclient.DefaultSections
import com.jocmp.feedbinclient.Sections
import com.jocmp.feedbinclient.api.FeedbinClient
import org.koin.dsl.module

internal val repositoryModule = module {
    single { FeedbinClient.create(context = get(), credentialsManager = get()) }
    single<Sections> { DefaultSections(database = get(), client = get()) }
    single { Authentication(client = get()) }
}
