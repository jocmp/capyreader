package com.capyreader.app.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdater {
    suspend fun update(context: Context) {
        HeadlinesWidget().updateAll(context)
    }
}
