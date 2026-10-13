package com.capyreader.app.refresher

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.capyreader.app.preferences.AppPreferences
import kotlinx.coroutines.flow.first
import java.time.Duration

data class RefreshStatus(
    val interval: RefreshInterval,
    val wifiOnly: Boolean,
    val jobState: WorkInfo.State?,
    val nextRunAt: Long?,
    val stopReason: Int?,
    val standbyBucket: Int,
    val ignoringBatteryOptimizations: Boolean,
    val backgroundRestricted: Boolean,
    val notificationsEnabled: Boolean,
) {
    fun isLate(gap: Duration): Boolean {
        val expected = interval.toDuration ?: return false

        return gap > expected.multipliedBy(2)
    }
}

class RefreshStatusReader(
    private val context: Context,
    private val appPreferences: AppPreferences,
) {
    suspend fun read(): RefreshStatus {
        val workInfo = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(RefreshScheduler.WORK_NAME)
            .first()
            .firstOrNull()

        val powerManager = context.getSystemService(PowerManager::class.java)
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)

        return RefreshStatus(
            interval = appPreferences.refreshInterval.get(),
            wifiOnly = appPreferences.refreshOnWiFiOnly.get(),
            jobState = workInfo?.state,
            nextRunAt = workInfo?.nextScheduleTimeMillis?.takeIf { it != Long.MAX_VALUE },
            stopReason = workInfo?.stopReason?.takeIf { it != WorkInfo.STOP_REASON_NOT_STOPPED },
            standbyBucket = usageStatsManager.appStandbyBucket,
            ignoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName),
            backgroundRestricted = activityManager.isBackgroundRestricted,
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
        )
    }
}

val RefreshInterval.toDuration: Duration?
    get() {
        val (amount, unit) = toTime ?: return null

        return Duration.ofMillis(unit.toMillis(amount))
    }

private const val STANDBY_BUCKET_EXEMPTED = 5

fun standbyBucketName(bucket: Int): String {
    val name = when (bucket) {
        STANDBY_BUCKET_EXEMPTED -> "Exempted"
        UsageStatsManager.STANDBY_BUCKET_ACTIVE -> "Active"
        UsageStatsManager.STANDBY_BUCKET_WORKING_SET -> "Working set"
        UsageStatsManager.STANDBY_BUCKET_FREQUENT -> "Frequent"
        UsageStatsManager.STANDBY_BUCKET_RARE -> "Rare"
        UsageStatsManager.STANDBY_BUCKET_RESTRICTED -> "Restricted"
        else -> "Unknown"
    }

    return "$name ($bucket)"
}

fun stopReasonName(reason: Int): String {
    val name = when (reason) {
        WorkInfo.STOP_REASON_NOT_STOPPED -> "Not stopped"
        WorkInfo.STOP_REASON_UNKNOWN -> "Unknown"
        WorkInfo.STOP_REASON_CANCELLED_BY_APP -> "Cancelled by app"
        WorkInfo.STOP_REASON_PREEMPT -> "Preempted"
        WorkInfo.STOP_REASON_TIMEOUT -> "Timeout"
        WorkInfo.STOP_REASON_DEVICE_STATE -> "Device state"
        WorkInfo.STOP_REASON_CONSTRAINT_BATTERY_NOT_LOW -> "Battery low"
        WorkInfo.STOP_REASON_CONSTRAINT_CHARGING -> "Not charging"
        WorkInfo.STOP_REASON_CONSTRAINT_CONNECTIVITY -> "No network"
        WorkInfo.STOP_REASON_CONSTRAINT_DEVICE_IDLE -> "Device idle"
        WorkInfo.STOP_REASON_CONSTRAINT_STORAGE_NOT_LOW -> "Storage low"
        WorkInfo.STOP_REASON_QUOTA -> "Quota"
        WorkInfo.STOP_REASON_BACKGROUND_RESTRICTION -> "Background restriction"
        WorkInfo.STOP_REASON_APP_STANDBY -> "App standby"
        WorkInfo.STOP_REASON_USER -> "User"
        WorkInfo.STOP_REASON_SYSTEM_PROCESSING -> "System processing"
        WorkInfo.STOP_REASON_ESTIMATED_APP_LAUNCH_TIME_CHANGED -> "App launch time changed"
        else -> "Other"
    }

    return "$name ($reason)"
}
