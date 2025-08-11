package com.capyreader.app.preferences

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.MarkReadPosition
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.FontOption
import com.jocmp.capy.articles.FontSize
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.common.withIOContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class AppSettings(
    @SerialName("article_filter")
    val filter: ArticleFilter,
    @SerialName("account_id")
    val accountID: String,
    @SerialName("refresh_interval")
    val refreshInterval: RefreshInterval,
    @SerialName("article_id")
    val articleID: String,
    @SerialName("enable_crash_reporting")
    val crashReporting: Boolean,
    @SerialName("article_pin_top_bar")
    val pinTopToolbar: Boolean,
    @SerialName("theme")
    val theme: ThemeOption,
    @SerialName("open_links_internally")
    val openLinksInternally: Boolean,
    @SerialName("enable_sticky_full_content")
    val enableStickyFullContent: Boolean,
    @SerialName("enable_high_contrast_dark_theme")
    val enableHighContrastDarkTheme: Boolean,
    @SerialName("layout_preference")
    val layout: LayoutPreference,
    @SerialName("article_bottom_bar_actions")
    val bottomBarActions: Boolean,
    @SerialName("article_font_size")
    val fontSize: Int,
    @SerialName("article_font_family")
    val fontFamily: FontOption,
    @SerialName("article_top_swipe_gesture")
    val topSwipeGesture: ArticleVerticalSwipe,
    @SerialName("article_bottom_swipe_gesture")
    val bottomSwipeGesture: ArticleVerticalSwipe,
    @SerialName("article_image_visibility")
    val imageVisibility: ReaderImageVisibility,
    @SerialName("article_enable_paging_tap_gesture")
    val enablePagingTapGesture: Boolean,
    @SerialName("article_enable_horizontal_pagination")
    val enableHorizontalPagination: Boolean,
    @SerialName("article_improve_talkback")
    val improveTalkback: Boolean,
    @SerialName("article_list_back_action")
    val backAction: BackAction,
    @SerialName("article_list_unread_sort_order")
    val unreadSort: UnreadSortOrder,
    @SerialName("article_display_feed_name")
    val showFeedName: Boolean,
    @SerialName("article_display_feed_icons")
    val showFeedIcons: Boolean,
    @SerialName("article_display_show_summary")
    val showSummary: Boolean,
    @SerialName("article_display_image_preview")
    val imagePreview: ImagePreview,
    @SerialName("article_display_shorten_titles")
    val shortenTitles: Boolean,
    @SerialName("article_display_font_scale")
    val fontScale: ArticleListFontScale,
    @SerialName("article_list_mark_read_position")
    val markReadButtonPosition: MarkReadPosition,
    @SerialName("article_list_swipe_start")
    val swipeStart: RowSwipeOption,
    @SerialName("article_list_swipe_end")
    val swipeEnd: RowSwipeOption,
    @SerialName("article_list_swipe_bottom")
    val swipeBottom: ArticleListVerticalSwipe,
    @SerialName("article_list_confirm_mark_all_read")
    val confirmMarkAllRead: Boolean,
    @SerialName("article_list_mark_read_on_scroll")
    val markReadOnScroll: Boolean,
    @SerialName("after_read_all_behavior")
    val afterReadAllBehavior: AfterReadAllBehavior,
    val pinTags: Boolean,
    val pinSearches: Boolean,
    val pinTopLevelFeeds: Boolean,
) {
    val isLoggedIn
        get() = accountID.isNotBlank()
}

class AppPreferences(context: Context) {
    private val dataStore: DataStore<AppSettings> = MultiProcessDataStoreFactory.create(
        serializer = AppSettingsSerializer(),
        produceFile = {
            File(context.filesDir, "datastore/app.preferences_pb")
        }
    )

    val settings = dataStore.data

    suspend fun update(func: (AppSettings) -> AppSettings) {
        dataStore.updateData { settings ->
            func(settings)
        }
    }
}

class AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue = AppSettings(
        filter = ArticleFilter.default(),
        accountID = "",
        refreshInterval = RefreshInterval.default,
        articleID = "",
        crashReporting = false,
        pinTopToolbar = true,
        theme = ThemeOption.default,
        openLinksInternally = true,
        enableStickyFullContent = false,
        enableHighContrastDarkTheme = false,
        layout = LayoutPreference.RESPONSIVE,
        bottomBarActions = false,
        fontSize = FontSize.DEFAULT,
        fontFamily = FontOption.default,
        topSwipeGesture = ArticleVerticalSwipe.topSwipeDefault,
        bottomSwipeGesture = ArticleVerticalSwipe.bottomSwipeDefault,
        imageVisibility = ReaderImageVisibility.ALWAYS_SHOW,
        enablePagingTapGesture = false,
        enableHorizontalPagination = true,
        improveTalkback = false,
        backAction = BackAction.default,
        unreadSort = UnreadSortOrder.default,
        showFeedName = true,
        showFeedIcons = true,
        showSummary = true,
        imagePreview = ImagePreview.default,
        shortenTitles = true,
        fontScale = ArticleListFontScale.default,
        markReadButtonPosition = MarkReadPosition.default,
        swipeStart = RowSwipeOption.default,
        swipeEnd = RowSwipeOption.default,
        swipeBottom = ArticleListVerticalSwipe.default,
        confirmMarkAllRead = true,
        markReadOnScroll = false,
        afterReadAllBehavior = AfterReadAllBehavior.NOTHING,
        pinTags = true,
        pinSearches = true,
        pinTopLevelFeeds = true,
    )

    override suspend fun readFrom(input: InputStream): AppSettings =
        try {
            Json.decodeFromString(input.readBytes().decodeToString())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        withIOContext {
            output.write(
                Json.encodeToString(t)
                    .encodeToByteArray()
            )
        }
    }
}
