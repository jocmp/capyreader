package com.capyreader.app.common

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context

val Context.notificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.activityManager
    get() = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
