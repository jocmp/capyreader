package com.capyreader.app.common

import android.content.Context
import android.content.Intent
import com.google.android.gms.security.ProviderInstaller
import com.jocmp.capy.logging.CapyLog

class SecurityUpdater {
    fun updateAsync(context: Context) {
        ProviderInstaller.installIfNeededAsync(context, Listener())
    }

    private class Listener : ProviderInstaller.ProviderInstallListener {
        override fun onProviderInstalled() {
            CapyLog.info(TAG)
        }

        override fun onProviderInstallFailed(errorCode: Int, intent: Intent?) {
            CapyLog.warn(TAG, mapOf("error_code" to errorCode.toString()))
        }
    }

    companion object {
        const val TAG = "sec_update"
    }
}
