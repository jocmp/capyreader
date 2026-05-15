package com.capyreader.app.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.jocmp.capy.preferences.OfflineCacheSize

@Composable
fun OfflineCacheSize.label(): String = when (this) {
    OfflineCacheSize.Off -> stringResource(R.string.offline_cache_size_off)
    is OfflineCacheSize.Latest -> stringResource(R.string.offline_cache_size_latest, count)
}
