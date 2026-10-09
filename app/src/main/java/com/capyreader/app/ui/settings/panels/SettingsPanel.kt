package com.capyreader.app.ui.settings.panels

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Build
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
    data object Account : SettingsPanel(title = R.string.settings_account_title), Parcelable {
        override fun icon() = Icons.Rounded.AccountCircle
    }

    @Parcelize
    data object ArticleList : SettingsPanel(title = R.string.settings_article_list_title),
        Parcelable {
        override fun icon() = Icons.AutoMirrored.Rounded.ViewList
    }

    @Parcelize
    data object Reader : SettingsPanel(title = R.string.settings_reader_title), Parcelable {
        override fun icon() = Icons.AutoMirrored.Rounded.Article
    }

    @Parcelize
    data object Display : SettingsPanel(title = R.string.settings_panel_display_title), Parcelable {
        override fun icon() = Icons.Rounded.Palette
    }

    @Parcelize
    data object Notifications : SettingsPanel(title = R.string.settings_panel_notifications_title), Parcelable {
        override fun icon() = Icons.Rounded.Notifications
    }

    @Parcelize
    data object Advanced : SettingsPanel(title = R.string.settings_section_advanced), Parcelable {
        override fun icon() = Icons.Rounded.Build
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

    companion object {
        val items: List<SettingsPanel>
            get() = listOf(
                Account,
                ArticleList,
                Reader,
                Display,
                Notifications,
                Advanced,
                About,
            )
    }
}
