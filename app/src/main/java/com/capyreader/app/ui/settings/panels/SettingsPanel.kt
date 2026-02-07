package com.capyreader.app.ui.settings.panels

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.ui.graphics.vector.ImageVector
import com.capyreader.app.R
import kotlinx.parcelize.Parcelize

sealed class SettingsPanel(@StringRes val title: Int) {
    abstract fun icon(): ImageVector

    @Parcelize
    data object General : SettingsPanel(title = R.string.settings_panel_general_title), Parcelable {
        override fun icon() = Icons.Rounded.Build
    }

    @Parcelize
    data object Notifications : SettingsPanel(title = R.string.settings_panel_notifications_title), Parcelable {
        override fun icon() = Icons.Rounded.Notifications
    }

    @Parcelize
    data object Display : SettingsPanel(title = R.string.settings_panel_display_title), Parcelable {
        override fun icon() = Icons.Rounded.Palette
    }

    @Parcelize
    data object Gestures : SettingsPanel(title = R.string.settings_panel_gestures_title),
        Parcelable {
        override fun icon() = Icons.Rounded.Gesture
    }

    @Parcelize
    data object Account : SettingsPanel(title = R.string.settings_account_title), Parcelable {
        override fun icon() = Icons.Rounded.AccountCircle
    }

    @Parcelize
    data object About : SettingsPanel(title = R.string.settings_about_title), Parcelable {
        override fun icon() = Icons.Rounded.Info
    }

    @Parcelize
    data object UnreadBadges : SettingsPanel(title = R.string.settings_panel_unread_counts_title),
        Parcelable {
        override fun icon() = Icons.Rounded.Visibility
    }

    fun isNested() = !items.contains(this)

    companion object {
        val items: List<SettingsPanel>
            get() = listOf(
                General,
                Display,
                Gestures,
                Account,
                About,
            )
    }
}
