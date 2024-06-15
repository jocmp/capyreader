package com.jocmp.capyreader.sync

import com.jocmp.capy.Account
import com.jocmp.capy.common.isNetworkError

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReadSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val account by inject<Account>()

    override suspend fun doWork(): Result {
        val articleID = inputData.getString(ARTICLE_KEY) ?: return Result.failure()
        val markRead = inputData.getBoolean(MARK_READ_KEY, false)

        val result = if (markRead) {
            account.markRead(articleID)
        } else {
            account.markUnread(articleID)
        }

        return result
            .fold(
                onSuccess = { Result.success() },
                onFailure = {
                    return if (it.isNetworkError) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
    }

    companion object {
        const val ARTICLE_KEY = "article_id"
        const val MARK_READ_KEY = "mark_read"
    }
}
