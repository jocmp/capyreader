package com.capyreader.app.common

import android.content.Context
import android.os.StatFs
import com.jocmp.capy.Account

private const val MAX_CACHE_BYTES: Long = 2L * 1024L * 1024L * 1024L
private const val DEVICE_FRACTION: Double = 0.02

class OfflineStorage(private val context: Context) {
    fun limitBytes(): Long {
        val total = StatFs(context.filesDir.path).run { blockCountLong * blockSizeLong }
        val byFraction = (total * DEVICE_FRACTION).toLong()
        return minOf(MAX_CACHE_BYTES, byFraction)
    }

    fun usedBytes(account: Account): Long = account.offlineAssets.totalSizeBytes()
}
