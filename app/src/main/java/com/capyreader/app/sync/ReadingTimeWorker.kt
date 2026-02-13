package com.capyreader.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jocmp.capy.Account
import com.jocmp.capy.articles.ReadingTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReadingTimeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val account by inject<Account>()

    override suspend fun doWork(): Result {
        val batchSize = 500L
        var processed: Int

        do {
            val articles = account.database.articlesQueries
                .articlesWithMissingReadingTime(batchSize)
                .executeAsList()
            processed = articles.size

            articles.forEach { row ->
                val minutes = ReadingTime.calculate(row.content_html)
                if (minutes != null) {
                    account.database.articlesQueries
                        .updateReadingTime(
                            readingTimeMinutes = minutes,
                            articleID = row.id
                        )
                }
            }
        } while (processed >= batchSize.toInt())

        return Result.success()
    }
}
