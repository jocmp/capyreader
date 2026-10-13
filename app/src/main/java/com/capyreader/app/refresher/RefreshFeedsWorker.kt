package com.capyreader.app.refresher

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RefreshFeedsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val refresher by inject<FeedRefresher>()
    private val refreshLog by inject<RefreshLog>()

    override suspend fun doWork(): Result {
        CapyLog.info("refresh_feeds_worker:start")
        refreshLog.record(RefreshEvent.STARTED)

        return try {
            recordOutcome(refresher.refresh())
            CapyLog.info("refresh_feeds_worker:success")
            Result.success()
        } catch (e: CancellationException) {
            withContext(NonCancellable) {
                refreshLog.record(RefreshEvent.STOPPED, detail = stopReasonName(stopReason))
            }
            throw e
        } catch (e: Exception) {
            CapyLog.error("refresh_feeds_worker", e)
            refreshLog.record(RefreshEvent.FAILED, detail = e.toString())
            Result.failure()
        }
    }

    private suspend fun recordOutcome(outcome: RefreshOutcome) {
        when (outcome) {
            RefreshOutcome.SkippedWifiOnly -> refreshLog.record(RefreshEvent.SKIPPED_WIFI_ONLY)

            is RefreshOutcome.Refreshed -> refreshLog.record(
                RefreshEvent.REFRESHED,
                notifications = outcome.notifications,
            )

            is RefreshOutcome.Failed -> refreshLog.record(
                RefreshEvent.FAILED,
                notifications = outcome.notifications,
                detail = outcome.error.toString(),
            )
        }
    }
}
