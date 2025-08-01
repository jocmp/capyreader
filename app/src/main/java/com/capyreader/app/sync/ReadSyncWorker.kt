package com.capyreader.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jocmp.capy.Account
import com.jocmp.capy.common.isIOError
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReadSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val account by inject<Account>()

    override suspend fun doWork(): Result {
        if (SyncWorkers.isMaxAttemptMet(runAttemptCount)) {
            return Result.failure()
        }

        val articleIDs = inputData
            .getStringArray(ARTICLES_KEY)?.toList() ?: return Result.failure()
        val markRead = inputData.getBoolean(MARK_READ_KEY, false)

        if (articleIDs.isEmpty()) {
            return Result.failure()
        }

        val result = if (markRead) {
            account.markAllRead(articleIDs)
        } else {
            account.markUnread(articleIDs.first())
        }

        return result
            .fold(
                onSuccess = { Result.success() },
                onFailure = {
                    SyncLogger.logError(
                        error = it,
                        value = markRead,
                        workType = "read",
                        articleIDs = articleIDs
                    )
                    return if (it.isIOError) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
    }

    companion object {
        const val ARTICLES_KEY = "articles_key"
        const val MARK_READ_KEY = "mark_read"
    }
}
