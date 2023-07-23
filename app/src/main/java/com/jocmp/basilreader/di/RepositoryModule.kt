package com.jocmp.basilreader.di
import com.jocmp.feedbinclient.Authentication
import com.jocmp.feedbinclient.DefaultSubscriptions
import com.jocmp.feedbinclient.Subscriptions
import com.jocmp.feedbinclient.api.FeedbinClient
import org.koin.dsl.module

internal val repositoryModule = module {
    single { FeedbinClient.create(context = get(), credentialsManager = get()) }
    single<Subscriptions> { DefaultSubscriptions(database = get(), client = get()) }
    single { Authentication(client = get()) }
}
