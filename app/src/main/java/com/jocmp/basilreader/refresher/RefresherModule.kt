package com.jocmp.basilreader.refresher

import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val refresherModule = module {
    single { FeedRefresher(account = get()) }
    single { RefreshScheduler(get(), get()) }
    worker { RefreshFeedsWorker(get(), get()) }
}
