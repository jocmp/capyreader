package com.capyreader.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jocmp.capy.Account
import com.jocmp.capy.common.isIOError
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StarSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val account by inject<Account>()

    override suspend fun doWork(): Result {
        val articleID = inputData.getString(ARTICLE_KEY) ?: return Result.failure()
        val addStar = inputData.getBoolean(ADD_STAR_KEY, false)

        val result = if (addStar) {
            account.addStar(articleID)
        } else {
            account.removeStar(articleID)
        }

        return result
            .fold(
                onSuccess = { Result.success() },
                onFailure = {
                    SyncLogger.logError(
                        error = it,
                        value = addStar,
                        workType = "star",
                        articleIDs = listOf(articleID)
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
        const val ARTICLE_KEY = "article_id"
        const val ADD_STAR_KEY = "mark_read"
    }
}
