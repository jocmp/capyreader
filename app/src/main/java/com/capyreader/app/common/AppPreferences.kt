package com.capyreader.app.common

import android.content.Context
import androidx.preference.PreferenceManager
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.settings.ArticleVerticalSwipe
import com.capyreader.app.ui.settings.RowSwipeOption
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.FontOption
import com.jocmp.capy.articles.TextSize
import com.jocmp.capy.preferences.AndroidPreferenceStore
import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.PreferenceStore
import com.jocmp.capy.preferences.getEnum
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppPreferences(context: Context) {
    private val preferenceStore: PreferenceStore = AndroidPreferenceStore(
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    )

    val readerOptions = ReaderOptions(preferenceStore)

    val articleListOptions = ArticleListOptions(preferenceStore)

    val accountID: Preference<String>
        get() = preferenceStore.getString("account_id")

    val filter: Preference<ArticleFilter>
        get() = preferenceStore.getObject(
            key = "article_filter",
            defaultValue = ArticleFilter.default(),
            serializer = { Json.encodeToString(it) },
            deserializer = { Json.decodeFromString(it) }
        )

    val refreshInterval: Preference<RefreshInterval>
        get() = preferenceStore.getEnum("refresh_interval", RefreshInterval.default)

    val articleID: Preference<String>
        get() = preferenceStore.getString("article_id")

    val crashReporting: Preference<Boolean>
        get() = preferenceStore.getBoolean("enable_crash_reporting", false)

    val theme: Preference<ThemeOption>
        get() = preferenceStore.getEnum("theme", ThemeOption.default)

    val openLinksInternally: Preference<Boolean>
        get() = preferenceStore.getBoolean("open_links_internally", true)

    val enableStickyFullContent: Preference<Boolean>
        get() = preferenceStore.getBoolean("enable_sticky_full_content", false)

    fun clearAll() {
        preferenceStore.clearAll()
    }

    class ReaderOptions(private val preferenceStore: PreferenceStore) {
        val pinToolbars: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_pin_top_bar", true)

        val textSize: Preference<TextSize>
            get() = preferenceStore.getEnum("article_text_size", TextSize.default)

        val fontFamily: Preference<FontOption>
            get() = preferenceStore.getEnum("article_font_family", FontOption.default)

        val topSwipeGesture: Preference<ArticleVerticalSwipe>
            get() = preferenceStore.getEnum("article_top_swipe_gesture", ArticleVerticalSwipe.PREVIOUS_ARTICLE)

        val bottomSwipeGesture: Preference<ArticleVerticalSwipe>
            get() = preferenceStore.getEnum("article_bottom_swipe_gesture", ArticleVerticalSwipe.NEXT_ARTICLE)
    }

    class ArticleListOptions(private val preferenceStore: PreferenceStore) {
        val backAction: Preference<BackAction>
            get() = preferenceStore.getEnum("article_list_back_action", BackAction.default)

        val unreadSort: Preference<UnreadSortOrder>
            get() = preferenceStore.getEnum("article_list_unread_sort_order", UnreadSortOrder.default)

        val showFeedName: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_display_feed_name", true)

        val showFeedIcons: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_display_feed_icons", true)

        val showSummary: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_display_show_summary", true)

        val imagePreview: Preference<ImagePreview>
            get() = preferenceStore.getEnum("article_display_image_preview", ImagePreview.default)

        val fontScale: Preference<ArticleListFontScale>
            get() = preferenceStore.getEnum("article_display_font_scale", ArticleListFontScale.default)

        val swipeStart: Preference<RowSwipeOption>
            get() = preferenceStore.getEnum("article_list_swipe_start", RowSwipeOption.default)

        val swipeEnd: Preference<RowSwipeOption>
            get() = preferenceStore.getEnum("article_list_swipe_end", RowSwipeOption.default)
    }
}
