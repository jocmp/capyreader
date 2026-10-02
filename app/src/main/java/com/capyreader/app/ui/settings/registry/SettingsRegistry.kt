package com.capyreader.app.ui.settings.registry

import androidx.annotation.StringRes
import com.capyreader.app.R
import com.capyreader.app.ui.settings.panels.SettingsPanel
import com.jocmp.capy.accounts.Source

data class SettingsSection(
    @StringRes val title: Int?,
    val settings: List<Setting>,
)

data class SettingsEnvironment(
    val source: Source,
    val crashReporting: Boolean,
    val debug: Boolean,
)

data class SettingsSearchResult(
    val panel: SettingsPanel,
    val setting: Setting?,
    val title: String,
    val path: List<String>,
)

object SettingsRegistry {
    fun sections(panel: SettingsPanel, environment: SettingsEnvironment): List<SettingsSection> {
        return declaredSections(panel)
            .map { section ->
                section.copy(settings = section.settings.filter { isAvailable(it, environment) })
            }
            .filter { it.settings.isNotEmpty() }
    }

    fun declaredSections(panel: SettingsPanel): List<SettingsSection> {
        return when (panel) {
            SettingsPanel.Account -> listOf(
                SettingsSection(
                    title = R.string.settings_section_account,
                    settings = listOf(Setting.ACCOUNT, Setting.SERVER),
                ),
                SettingsSection(
                    title = R.string.settings_section_refresh,
                    settings = listOf(
                        Setting.REFRESH_INTERVAL,
                        Setting.REFRESH_ON_WIFI_ONLY,
                        Setting.LAST_REFRESHED,
                    ),
                ),
                SettingsSection(
                    title = R.string.settings_section_import,
                    settings = listOf(Setting.OPML_IMPORT),
                ),
                SettingsSection(
                    title = R.string.settings_section_export,
                    settings = listOf(Setting.OPML_EXPORT, Setting.STARRED_EXPORT),
                ),
                SettingsSection(
                    title = null,
                    settings = listOf(Setting.REMOVE_ACCOUNT),
                ),
            )

            SettingsPanel.ArticleList -> listOf(
                SettingsSection(
                    title = R.string.article_list_sort_title,
                    settings = listOf(Setting.SORT_ORDER),
                ),
                SettingsSection(
                    title = R.string.settings_section_layout,
                    settings = listOf(
                        Setting.ARTICLE_LIST_PREVIEW,
                        Setting.ARTICLE_LIST_FONT_SIZE,
                        Setting.SHOW_FEED_NAME,
                        Setting.SHOW_FEED_ICONS,
                        Setting.SHOW_SUMMARY,
                        Setting.SHORTEN_TITLES,
                        Setting.IMAGE_PREVIEW,
                    ),
                ),
                SettingsSection(
                    title = R.string.settings_panel_gestures_title,
                    settings = listOf(
                        Setting.LIST_SWIPE_START,
                        Setting.LIST_SWIPE_END,
                        Setting.LIST_SWIPE_UP,
                        Setting.BACK_ACTION,
                        Setting.MARK_READ_ON_SCROLL,
                    ),
                ),
                SettingsSection(
                    title = R.string.settings_section_mark_all_as_read,
                    settings = listOf(
                        Setting.CONFIRM_MARK_ALL_READ,
                        Setting.AFTER_READ_ALL,
                        Setting.MARK_ALL_READ_POSITION,
                    ),
                ),
                SettingsSection(
                    title = null,
                    settings = listOf(Setting.FILTERS),
                ),
            )

            SettingsPanel.Reader -> listOf(
                SettingsSection(
                    title = null,
                    settings = listOf(Setting.READER_STYLE),
                ),
                SettingsSection(
                    title = R.string.settings_section_content,
                    settings = listOf(
                        Setting.READER_IMAGES,
                        Setting.STICKY_FULL_CONTENT,
                        Setting.PIN_TOOLBARS,
                    ),
                ),
                SettingsSection(
                    title = R.string.settings_section_browser,
                    settings = listOf(Setting.IN_APP_BROWSER),
                ),
                SettingsSection(
                    title = R.string.settings_panel_gestures_title,
                    settings = listOf(
                        Setting.READER_SWIPE_DOWN,
                        Setting.READER_SWIPE_UP,
                        Setting.HORIZONTAL_PAGINATION,
                    ),
                ),
            )

            SettingsPanel.Display -> listOf(
                SettingsSection(
                    title = R.string.theme_menu_label,
                    settings = listOf(
                        Setting.THEME_MODE,
                        Setting.THEME,
                        Setting.PURE_BLACK,
                        Setting.ACCENT_COLORS,
                    ),
                ),
                SettingsSection(
                    title = R.string.settings_section_e_ink,
                    settings = listOf(
                        Setting.REDUCE_MOTION,
                        Setting.TAP_TO_PAGE,
                        Setting.E_INK_SCROLLBAR,
                        Setting.PAGE_TURN_KEYS,
                    ),
                ),
            )

            SettingsPanel.Advanced -> listOf(
                SettingsSection(
                    title = R.string.settings_section_storage,
                    settings = listOf(Setting.AUTO_DELETE, Setting.CLEAR_ARTICLES),
                ),
                SettingsSection(
                    title = R.string.settings_section_privacy,
                    settings = listOf(Setting.CRASH_REPORTING, Setting.CRASH_LOGS),
                ),
                SettingsSection(
                    title = null,
                    settings = listOf(Setting.TEST_NOTIFICATION),
                ),
            )

            SettingsPanel.Notifications,
            SettingsPanel.About -> emptyList()
        }
    }

    fun isAvailable(setting: Setting, environment: SettingsEnvironment): Boolean {
        return when (setting) {
            Setting.ACCOUNT,
            Setting.SERVER -> environment.source != Source.LOCAL

            Setting.OPML_IMPORT,
            Setting.FILTERS -> environment.source == Source.LOCAL

            Setting.CRASH_REPORTING -> environment.crashReporting
            Setting.TEST_NOTIFICATION -> environment.debug
            else -> true
        }
    }

    @StringRes
    fun title(setting: Setting, source: Source): Int? {
        return when (setting) {
            Setting.ACCOUNT -> R.string.settings_section_account
            Setting.SERVER -> R.string.settings_section_account_server
            Setting.REFRESH_INTERVAL -> R.string.refresh_feeds_menu_label
            Setting.REFRESH_ON_WIFI_ONLY -> R.string.settings_refresh_on_wifi_only
            Setting.LAST_REFRESHED -> R.string.settings_last_refreshed
            Setting.OPML_IMPORT -> R.string.opml_import_button_text
            Setting.OPML_EXPORT -> R.string.opml_export_button_text
            Setting.STARRED_EXPORT -> R.string.starred_export_button_text
            Setting.REMOVE_ACCOUNT -> removeAccountTitle(source)
            Setting.SORT_ORDER -> R.string.article_list_sort_title
            Setting.ARTICLE_LIST_PREVIEW -> null
            Setting.ARTICLE_LIST_FONT_SIZE -> R.string.article_font_scale_label
            Setting.SHOW_FEED_NAME -> R.string.settings_article_list_feed_name
            Setting.SHOW_FEED_ICONS -> R.string.settings_article_list_feed_icons
            Setting.SHOW_SUMMARY -> R.string.settings_article_list_summary
            Setting.SHORTEN_TITLES -> R.string.settings_article_list_shorten_titles
            Setting.IMAGE_PREVIEW -> R.string.image_preview_label
            Setting.LIST_SWIPE_START -> R.string.settings_gestures_list_row_swipe_start
            Setting.LIST_SWIPE_END -> R.string.settings_gestures_list_row_swipe_end
            Setting.LIST_SWIPE_UP -> R.string.settings_gestures_list_swipe_up
            Setting.BACK_ACTION -> R.string.settings_gestures_list_back_navigation_action
            Setting.MARK_READ_ON_SCROLL -> R.string.settings_mark_read_on_scroll
            Setting.CONFIRM_MARK_ALL_READ -> R.string.settings_confirm_mark_all_read
            Setting.AFTER_READ_ALL -> R.string.after_read_all_behavior_label
            Setting.MARK_ALL_READ_POSITION -> R.string.mark_all_read_button_position
            Setting.FILTERS -> R.string.filters_title
            Setting.READER_STYLE -> R.string.article_style_options
            Setting.READER_IMAGES -> R.string.reader_image_visibility_label
            Setting.STICKY_FULL_CONTENT -> R.string.settings_option_full_content_title
            Setting.PIN_TOOLBARS -> R.string.settings_options_reader_pin_top_toolbar
            Setting.IN_APP_BROWSER -> R.string.settings_option_in_app_browser
            Setting.READER_SWIPE_DOWN -> R.string.settings_gestures_reader_swipe_down
            Setting.READER_SWIPE_UP -> R.string.settings_gestures_reader_swipe_up
            Setting.HORIZONTAL_PAGINATION -> R.string.settings_gestures_enable_horizontal_pagination_title
            Setting.REDUCE_MOTION -> R.string.settings_reduce_motion_title
            Setting.TAP_TO_PAGE -> R.string.settings_e_ink_tap_to_page_title
            Setting.E_INK_SCROLLBAR -> R.string.settings_e_ink_scrollbar_title
            Setting.PAGE_TURN_KEYS -> R.string.settings_e_ink_page_turn_keys_title
            Setting.THEME_MODE -> R.string.theme_mode_label
            Setting.THEME -> R.string.theme_menu_label
            Setting.PURE_BLACK -> R.string.settings_pure_black_dark_mode
            Setting.ACCENT_COLORS -> R.string.settings_accent_colors
            Setting.AUTO_DELETE -> R.string.settings_auto_delete_articles_title
            Setting.CLEAR_ARTICLES -> R.string.settings_clear_all_articles_button
            Setting.CRASH_REPORTING -> R.string.crash_reporting_checkbox_title
            Setting.CRASH_LOGS -> R.string.crash_log_export_item_title
            Setting.TEST_NOTIFICATION -> null
        }
    }

    fun keywords(setting: Setting): List<Int> {
        return when (setting) {
            Setting.SORT_ORDER -> listOf(
                R.string.article_list_sort_newest_first,
                R.string.article_list_sort_oldest_first,
            )

            Setting.READER_STYLE -> listOf(
                R.string.article_font_menu_label,
                R.string.article_font_scale_label,
                R.string.article_style_text_section,
                R.string.article_style_title_section,
                R.string.article_style_title_follow_body_font,
            )

            Setting.THEME_MODE -> listOf(
                R.string.theme_mode_light,
                R.string.theme_mode_dark,
                R.string.theme_mode_system,
            )

            Setting.PURE_BLACK -> listOf(R.string.theme_mode_dark)
            Setting.MARK_ALL_READ_POSITION -> listOf(
                R.string.mark_read_position_toolbar,
                R.string.mark_read_position_floating_action_button,
            )
            Setting.STICKY_FULL_CONTENT -> listOf(R.string.settings_option_full_content_subtitle)
            Setting.HORIZONTAL_PAGINATION -> listOf(R.string.settings_gestures_enable_horizontal_pagination_subtitle)
            Setting.REDUCE_MOTION -> listOf(R.string.settings_reduce_motion_subtitle)
            Setting.TAP_TO_PAGE -> listOf(R.string.settings_e_ink_tap_to_page_subtitle)
            Setting.E_INK_SCROLLBAR -> listOf(R.string.settings_e_ink_scrollbar_subtitle)
            Setting.PAGE_TURN_KEYS -> listOf(R.string.settings_e_ink_page_turn_keys_subtitle)
            Setting.AUTO_DELETE -> listOf(R.string.settings_option_auto_delete_articles_title)
            Setting.FILTERS -> listOf(R.string.filters_supporting_text)
            Setting.CRASH_LOGS -> listOf(R.string.crash_log_export_item_subtitle)
            else -> emptyList()
        }
    }

    fun search(
        query: String,
        environment: SettingsEnvironment,
        resolve: (Int) -> String,
    ): List<SettingsSearchResult> {
        val terms = query.trim().lowercase().split(whitespace).filter { it.isNotEmpty() }

        if (terms.isEmpty()) {
            return emptyList()
        }

        val panelResults = SettingsPanel.items.mapNotNull { panel ->
            val title = resolve(panel.title)

            if (matches(terms, listOf(title))) {
                SettingsSearchResult(
                    panel = panel,
                    setting = null,
                    title = title,
                    path = emptyList(),
                )
            } else {
                null
            }
        }

        val settingResults = SettingsPanel.items.flatMap { panel ->
            sections(panel, environment).flatMap { section ->
                val sectionTitle = section.title?.let(resolve)

                section.settings.mapNotNull { setting ->
                    val titleRes = title(setting, environment.source) ?: return@mapNotNull null
                    val title = resolve(titleRes)
                    val haystack = listOf(title) +
                            keywords(setting).map(resolve) +
                            listOfNotNull(sectionTitle)

                    if (matches(terms, haystack)) {
                        SettingsSearchResult(
                            panel = panel,
                            setting = setting,
                            title = title,
                            path = listOf(resolve(panel.title)) + listOfNotNull(sectionTitle),
                        )
                    } else {
                        null
                    }
                }
            }
        }

        return (panelResults + settingResults).sortedBy { rank(terms, it.title) }
    }

    private fun matches(terms: List<String>, haystack: List<String>): Boolean {
        val candidates = haystack.map { it.lowercase() }

        return terms.all { term -> candidates.any { it.contains(term) } }
    }

    private fun rank(terms: List<String>, title: String): Int {
        val normalized = title.lowercase()

        return when {
            normalized.startsWith(terms.first()) -> 0
            terms.all { normalized.contains(it) } -> 1
            else -> 2
        }
    }

    @StringRes
    private fun removeAccountTitle(source: Source): Int {
        if (source == Source.LOCAL) {
            return R.string.settings_remove_account_button_local
        }

        return R.string.settings_remove_account_button_service
    }

    private val whitespace = Regex("\\s+")
}
