package com.capyreader.app.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import com.capyreader.app.common.FeedGroup
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.MarkReadPosition
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.FontOption
import com.jocmp.capy.articles.FontSize
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.preferences.AndroidPreferenceStore
import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.PreferenceStore
import com.jocmp.capy.preferences.getEnum
import kotlinx.serialization.json.Json

class AppPreferences(context: Context) {
    private val preferenceStore: PreferenceStore = AndroidPreferenceStore(
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    )

    val readerOptions = ReaderOptions(preferenceStore)

    val articleListOptions = ArticleListOptions(preferenceStore)

    val isLoggedIn
        get() = accountID.get().isNotBlank()

    val accountID: Preference<String>
        get() = preferenceStore.getString("account_id")

    val filter: Preference<ArticleFilter>
        get() = preferenceStore.getObject(
            key = "article_filter",
            defaultValue = ArticleFilter.default(),
            serializer = { Json.encodeToString(it) },
            deserializer = {
                try {
                    Json.decodeFromString(it)
                } catch (e: Throwable) {
                    ArticleFilter.default()
                }
            }
        )

    val refreshInterval: Preference<RefreshInterval>
        get() = preferenceStore.getEnum("refresh_interval", RefreshInterval.default)

    val crashReporting: Preference<Boolean>
        get() = preferenceStore.getBoolean("enable_crash_reporting", false)

    val theme: Preference<ThemeOption>
        get() = preferenceStore.getEnum("theme", ThemeOption.default)

    val openLinksInternally: Preference<Boolean>
        get() = preferenceStore.getBoolean("open_links_internally", true)

    val enableStickyFullContent: Preference<Boolean>
        get() = preferenceStore.getBoolean("enable_sticky_full_content", false)

    val enableHighContrastDarkTheme: Preference<Boolean>
        get() = preferenceStore.getBoolean("enable_high_contrast_dark_theme", false)

    val layout: Preference<LayoutPreference>
        get() = preferenceStore.getEnum("layout_preference", LayoutPreference.RESPONSIVE)

    fun pinFeedGroup(type: FeedGroup): Preference<Boolean> {
        return preferenceStore.getBoolean("feed_group_${type.toString().lowercase()}", true)
    }

    fun clearAll() {
        preferenceStore.clearAll()
    }

    class ReaderOptions(private val preferenceStore: PreferenceStore) {
        val pinTopToolbar: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_pin_top_bar", true)

        val bottomBarActions: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_bottom_bar_actions", false)

        val fontSize: Preference<Int>
            get() = preferenceStore.getInt("article_font_size", FontSize.DEFAULT)

        val fontFamily: Preference<FontOption>
            get() = preferenceStore.getEnum("article_font_family", FontOption.default)

        val topSwipeGesture: Preference<ArticleVerticalSwipe>
            get() = preferenceStore.getEnum(
                "article_top_swipe_gesture",
                ArticleVerticalSwipe.topSwipeDefault
            )

        val bottomSwipeGesture: Preference<ArticleVerticalSwipe>
            get() = preferenceStore.getEnum(
                "article_bottom_swipe_gesture",
                ArticleVerticalSwipe.bottomSwipeDefault
            )

        val imageVisibility: Preference<ReaderImageVisibility>
            get() = preferenceStore.getEnum(
                "article_image_visibility",
                ReaderImageVisibility.ALWAYS_SHOW
            )

        val enablePagingTapGesture: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_enable_paging_tap_gesture", false)

        val enableHorizontaPagination: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_enable_horizontal_pagination", true)

        val improveTalkback: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_improve_talkback", false)
    }

    class ArticleListOptions(private val preferenceStore: PreferenceStore) {
        val backAction: Preference<BackAction>
            get() = preferenceStore.getEnum("article_list_back_action", BackAction.default)

        val unreadSort: Preference<UnreadSortOrder>
            get() = preferenceStore.getEnum(
                "article_list_unread_sort_order",
                UnreadSortOrder.default
            )

        val showFeedName: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_display_feed_name", true)

        val showFeedIcons: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_display_feed_icons", true)

        val showSummary: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_display_show_summary", true)

        val imagePreview: Preference<ImagePreview>
            get() = preferenceStore.getEnum("article_display_image_preview", ImagePreview.default)

        val shortenTitles: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_display_shorten_titles", true)

        val fontScale: Preference<ArticleListFontScale>
            get() = preferenceStore.getEnum(
                "article_display_font_scale",
                ArticleListFontScale.default
            )

        val markReadButtonPosition: Preference<MarkReadPosition>
            get() = preferenceStore.getEnum(
                "article_list_mark_read_position",
                MarkReadPosition.default
            )

        val swipeStart: Preference<RowSwipeOption>
            get() = preferenceStore.getEnum("article_list_swipe_start", RowSwipeOption.default)

        val swipeEnd: Preference<RowSwipeOption>
            get() = preferenceStore.getEnum("article_list_swipe_end", RowSwipeOption.default)

        val swipeBottom: Preference<ArticleListVerticalSwipe>
            get() = preferenceStore.getEnum(
                "article_list_swipe_bottom",
                ArticleListVerticalSwipe.default
            )

        val confirmMarkAllRead: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_list_confirm_mark_all_read", true)

        val markReadOnScroll: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_list_mark_read_on_scroll", false)

        val afterReadAllBehavior: Preference<AfterReadAllBehavior>
            get() = preferenceStore.getEnum(
                "after_read_all_behavior",
                AfterReadAllBehavior.withPreviousPref(openNextFeedOnReadAll.get())
            )

        /** @deprecated */
        private val openNextFeedOnReadAll: Preference<Boolean>
            get() = preferenceStore.getBoolean("open_next_feed_on_read_all", false)

    }
}
