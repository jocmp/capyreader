package com.capyreader.app.ui.settings.registry

import androidx.compose.runtime.Composable
import com.capyreader.app.ui.settings.panels.AccentColorsRow
import com.capyreader.app.ui.settings.panels.AccountNameRow
import com.capyreader.app.ui.settings.panels.AccountServerRow
import com.capyreader.app.ui.settings.panels.AfterReadAllRow
import com.capyreader.app.ui.settings.panels.ArticleListFontSizeRow
import com.capyreader.app.ui.settings.panels.ArticleListPreviewRow
import com.capyreader.app.ui.settings.panels.AutoDeleteRow
import com.capyreader.app.ui.settings.panels.BackActionRow
import com.capyreader.app.ui.settings.panels.ClearArticlesRow
import com.capyreader.app.ui.settings.panels.ConfirmMarkAllReadRow
import com.capyreader.app.ui.settings.panels.CrashLogsRow
import com.capyreader.app.ui.settings.panels.CrashReportingRow
import com.capyreader.app.ui.settings.panels.EInkScrollbarRow
import com.capyreader.app.ui.settings.panels.FiltersRow
import com.capyreader.app.ui.settings.panels.HorizontalPaginationRow
import com.capyreader.app.ui.settings.panels.ImagePreviewRow
import com.capyreader.app.ui.settings.panels.InAppBrowserRow
import com.capyreader.app.ui.settings.panels.LastRefreshedRow
import com.capyreader.app.ui.settings.panels.ListSwipeEndRow
import com.capyreader.app.ui.settings.panels.ListSwipeStartRow
import com.capyreader.app.ui.settings.panels.ListSwipeUpRow
import com.capyreader.app.ui.settings.panels.MarkReadOnScrollRow
import com.capyreader.app.ui.settings.panels.OPMLExportRow
import com.capyreader.app.ui.settings.panels.OPMLImportRow
import com.capyreader.app.ui.settings.panels.PageTurnKeysRow
import com.capyreader.app.ui.settings.panels.PinToolbarsRow
import com.capyreader.app.ui.settings.panels.PureBlackRow
import com.capyreader.app.ui.settings.panels.ReaderImagesRow
import com.capyreader.app.ui.settings.panels.ReduceMotionRow
import com.capyreader.app.ui.settings.panels.ReaderStyleRow
import com.capyreader.app.ui.settings.panels.ReaderSwipeDownRow
import com.capyreader.app.ui.settings.panels.ReaderSwipeUpRow
import com.capyreader.app.ui.settings.panels.RefreshIntervalRow
import com.capyreader.app.ui.settings.panels.RefreshOnWiFiOnlyRow
import com.capyreader.app.ui.settings.panels.RemoveAccountRow
import com.capyreader.app.ui.settings.panels.ShortenTitlesRow
import com.capyreader.app.ui.settings.panels.ShowFeedIconsRow
import com.capyreader.app.ui.settings.panels.ShowFeedNameRow
import com.capyreader.app.ui.settings.panels.ShowSummaryRow
import com.capyreader.app.ui.settings.panels.SortOrderSetting
import com.capyreader.app.ui.settings.panels.StarredExportRow
import com.capyreader.app.ui.settings.panels.StickyFullContentRow
import com.capyreader.app.ui.settings.panels.TapToPageRow
import com.capyreader.app.ui.settings.panels.TestNotificationSettingRow
import com.capyreader.app.ui.settings.panels.ThemeModeRow
import com.capyreader.app.ui.settings.panels.ThemeRow

@Composable
fun SettingContent(
    setting: Setting,
    onRemoveAccount: () -> Unit,
) {
    when (setting) {
        Setting.ACCOUNT -> AccountNameRow()
        Setting.SERVER -> AccountServerRow()
        Setting.REFRESH_INTERVAL -> RefreshIntervalRow()
        Setting.REFRESH_ON_WIFI_ONLY -> RefreshOnWiFiOnlyRow()
        Setting.LAST_REFRESHED -> LastRefreshedRow()
        Setting.OPML_IMPORT -> OPMLImportRow()
        Setting.OPML_EXPORT -> OPMLExportRow()
        Setting.STARRED_EXPORT -> StarredExportRow()
        Setting.REMOVE_ACCOUNT -> RemoveAccountRow(onRemoveAccount = onRemoveAccount)
        Setting.SORT_ORDER -> SortOrderSetting()
        Setting.ARTICLE_LIST_PREVIEW -> ArticleListPreviewRow()
        Setting.ARTICLE_LIST_FONT_SIZE -> ArticleListFontSizeRow()
        Setting.SHOW_FEED_NAME -> ShowFeedNameRow()
        Setting.SHOW_FEED_ICONS -> ShowFeedIconsRow()
        Setting.SHOW_SUMMARY -> ShowSummaryRow()
        Setting.SHORTEN_TITLES -> ShortenTitlesRow()
        Setting.IMAGE_PREVIEW -> ImagePreviewRow()
        Setting.LIST_SWIPE_START -> ListSwipeStartRow()
        Setting.LIST_SWIPE_END -> ListSwipeEndRow()
        Setting.LIST_SWIPE_UP -> ListSwipeUpRow()
        Setting.BACK_ACTION -> BackActionRow()
        Setting.MARK_READ_ON_SCROLL -> MarkReadOnScrollRow()
        Setting.CONFIRM_MARK_ALL_READ -> ConfirmMarkAllReadRow()
        Setting.AFTER_READ_ALL -> AfterReadAllRow()
        Setting.FILTERS -> FiltersRow()
        Setting.READER_STYLE -> ReaderStyleRow()
        Setting.READER_IMAGES -> ReaderImagesRow()
        Setting.STICKY_FULL_CONTENT -> StickyFullContentRow()
        Setting.PIN_TOOLBARS -> PinToolbarsRow()
        Setting.IN_APP_BROWSER -> InAppBrowserRow()
        Setting.READER_SWIPE_DOWN -> ReaderSwipeDownRow()
        Setting.READER_SWIPE_UP -> ReaderSwipeUpRow()
        Setting.HORIZONTAL_PAGINATION -> HorizontalPaginationRow()
        Setting.THEME_MODE -> ThemeModeRow()
        Setting.THEME -> ThemeRow()
        Setting.PURE_BLACK -> PureBlackRow()
        Setting.ACCENT_COLORS -> AccentColorsRow()
        Setting.REDUCE_MOTION -> ReduceMotionRow()
        Setting.TAP_TO_PAGE -> TapToPageRow()
        Setting.E_INK_SCROLLBAR -> EInkScrollbarRow()
        Setting.PAGE_TURN_KEYS -> PageTurnKeysRow()
        Setting.AUTO_DELETE -> AutoDeleteRow()
        Setting.CLEAR_ARTICLES -> ClearArticlesRow()
        Setting.CRASH_REPORTING -> CrashReportingRow()
        Setting.CRASH_LOGS -> CrashLogsRow()
        Setting.TEST_NOTIFICATION -> TestNotificationSettingRow()
    }
}
