package com.jocmp.capyreader.sync

import com.jocmp.capy.Account
import com.jocmp.capy.common.isNetworkError

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
        const val ADD_STAR_KEY = "mark_read"
    }
}
