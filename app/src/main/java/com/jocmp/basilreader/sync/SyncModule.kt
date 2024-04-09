package com.jocmp.basilreader.sync

import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val syncModule = module {
    worker { ReadSyncWorker(get(), get()) }
    worker { StarSyncWorker(get(), get()) }
}
