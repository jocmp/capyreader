package com.capyreader.app.sync

object SyncWorkers {
    fun isMaxAttemptMet(count: Int): Boolean {
        return count >= 5
    }
}