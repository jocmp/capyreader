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
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.articles.TextAlignment
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

    val articleID: Preference<String>
        get() = preferenceStore.getString("article_id")

    val crashReporting: Preference<Boolean>
        get() = preferenceStore.getBoolean("enable_crash_reporting", false)

    val themeMode: Preference<ThemeMode>
        get() = preferenceStore.getEnum("theme_mode", ThemeMode.default)
    
    val appTheme: Preference<AppTheme>
        get() = preferenceStore.getEnum("app_theme", AppTheme.default)
    
    val pureBlackDarkMode: Preference<Boolean>
        get() = preferenceStore.getBoolean("pure_black_dark_mode", false)

    val openLinksInternally: Preference<Boolean>
        get() = preferenceStore.getBoolean("open_links_internally", true)

    val enableStickyFullContent: Preference<Boolean>
        get() = preferenceStore.getBoolean("enable_sticky_full_content", false)


    val layout: Preference<LayoutPreference>
        get() = preferenceStore.getEnum("layout_preference", LayoutPreference.RESPONSIVE)

    fun pinFeedGroup(type: FeedGroup): Preference<Boolean> {
        return preferenceStore.getBoolean("feed_group_${type.toString().lowercase()}", true)
    }

    val showTodayFilter: Preference<Boolean>
        get() = preferenceStore.getBoolean("show_today_filter", true)

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

        val titleTextAlignment: Preference<TextAlignment>
            get() = preferenceStore.getEnum("article_title_text_alignment", TextAlignment.default)

        val titleFontSize: Preference<Int>
            get() = preferenceStore.getInt("article_title_font_size", FontSize.TITLE_DEFAULT)

        val titleFollowsBodyFont: Preference<Boolean>
            get() = preferenceStore.getBoolean("article_title_follows_body_font", false)
    }

    class ArticleListOptions(private val preferenceStore: PreferenceStore) {
        val backAction: Preference<BackAction>
            get() = preferenceStore.getEnum("article_list_back_action", BackAction.default)

        val sortOrder: Preference<SortOrder>
            get() = preferenceStore.getEnum(
                "article_list_sort_order",
                SortOrder.default
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
                AfterReadAllBehavior.default
            )
    }
}
