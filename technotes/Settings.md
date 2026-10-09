# Settings

Settings are declared in one registry and rendered from it. The registry is the single place that says which settings exist, where they live, and when they're available. Panels, search, and jump-to-setting all read from it.

## Why

Before the registry, every panel was a hand-written composable. Nothing knew what settings existed, so:

- Search wasn't possible without a second, hand-maintained index.
- The hierarchy drifted. Settings were grouped by *kind* (Display, Gestures) instead of *where they take effect*, which spread one surface across several panels:
  - Article list: sort under General, display under Display > Article List (two levels deep, linked from the Theme section), swipes under Gestures.
  - Reader: General > Reader, Display > Reader, Gestures > Reader, and typography only in the reader's bottom sheet.
  - General > "Last Updated" headed the refresh interval, Wi-Fi only, Notifications and Filters.
  - Notifications was only reachable through a row inside that section.
  - "Mark as read on scroll" sat under "Mark All As Read". "Advanced" mixed crash logs, retention, and a destructive button.

Android's system Settings search indexes only system apps (`SearchIndexablesProvider` needs a signature permission), so search has to be in-app.

## Prior art

- AOSP Settings: each screen registers a `SEARCH_INDEX_DATA_PROVIDER` (`BaseSearchIndexProvider`) built from its preference declarations. Search results open the screen with `EXTRA_FRAGMENT_ARG_KEY`, and `HighlightablePreferenceGroupAdapter` scrolls to and highlights the row.
- Mihon: each settings screen declares its rows as data through `getPreferences()` instead of hand-laying them out.

Capy follows the same loop (declare, index, jump and highlight), adapted to Compose and the existing view models.

## Shape

`app/src/main/java/com/capyreader/app/ui/settings/registry/`

| Piece             | Role                                                                                                    |
|-------------------|---------------------------------------------------------------------------------------------------------|
| `Setting`         | Enum of every setting row. Carries the searchable title (and optional keywords).                        |
| `SettingsRegistry`| Panel -> sections -> settings. Also answers availability (account source, flavor, debug) and search.    |
| `SettingContent`  | Exhaustive `when (setting)` that renders a row. The compiler catches a setting without a renderer.      |
| `RegistryPanel`   | Renders a registry-driven panel: filters unavailable settings, drops empty sections, handles highlight. |

To add a setting: add a `Setting` entry, place it in a section in `SettingsRegistry`, and add its branch to `SettingContent`. Moving a setting is a one-line change in the registry.

Row state still comes from the existing view models (`GeneralSettingsViewModel`, `DisplaySettingsViewModel`, `GesturesSettingsViewModel`, `AccountSettingsViewModel`), fetched with `koinViewModel()` inside each row. All rows share the settings entry's `ViewModelStoreOwner`, so they share one instance per view model. The view model names predate the new panels. Renaming them is cosmetic and was left out.

### Availability

Static conditions live in the registry (`isAvailable`), so search results and section headers agree with what's on screen:

- Local accounts only: Filters, OPML import
- Service accounts only: account name, server
- gplay flavor only: crash reporting
- Debug builds only: test notification

Conditions that depend on live state stay inside the row. For example, accent colors only show for themes that support them, and the server row hides a blank URL.

### Custom panels

Notifications, Unread Badges and About are lists of feeds or links rather than settings, so they stay hand-written. Search still matches the Notifications and About panel titles. Unread Badges is not a top-level panel; the `UNREAD_BADGES` row in Article List opens it through `RegistryPanel`'s `onNavigate`.

## Hierarchy

Grouped by where the setting takes effect.

- **Account**
  - Account: service and account name, server
  - Refresh: refresh interval, Wi-Fi only, last updated
  - Import (local): OPML import
  - Export: OPML, starred articles
  - Log out / delete account
- **Article List**
  - Article Sort: newest/oldest per status
  - Layout: live preview, font size, feed name, feed icons, summary, shorten titles, image preview
  - Gestures: swipe right, swipe left, swipe up, back navigation, mark as read on scroll
  - Mark All As Read: confirm, after read all
  - Unread Badges: badge style, per-feed and per-saved-search toggles (sub-panel)
  - Filters (local)
- **Reader**
  - Text and Title: the same typography controls as the reader's style sheet
  - Content: images, sticky full content, pin toolbars
  - Browser: in-app browser
  - Gestures: swipe down, swipe up, horizontal scroll
- **Display & Appearance**
  - Theme: mode, theme, pure black, accent colors
  - E Ink: reduce animations, tap to turn pages, page turn buttons (see `technotes/E Ink.md`)
- **Notifications**: top level. It stays disabled until periodic refresh is on, and it asks for the notification permission first.
- **Advanced**
  - Storage: keep read articles, clear all articles
  - Privacy: crash reporting (gplay), share crash logs
- **About**

The Gestures and General panels are gone; each gesture lives with the screen it affects.

## Search

The search field sits at the top of the settings list. It matches setting titles, keywords, section titles and panel titles against the resolved (translated) strings. Each result shows its path, for example "Article List › Gestures". Tapping one opens the panel, scrolls to the row, and briefly highlights it.

Only settings available for the current account and build show up.

### Focus

`ThreePaneScaffold` requests focus on the current destination pane whenever it changes (`PaneScaffoldDirective.shouldAutoFocusCurrentDestination`, on by default). The pane's `FocusRequester` sits on a non-focusable wrapper, so the request falls through to the first focusable descendant. In touch mode, clickables are not focusable (`Focusability.SystemDefined`), which leaves the search field as the only candidate: it grabbed focus and raised the keyboard every time Settings opened.

The settings list and each detail panel wrap their content in a `focusTarget()` that can hold focus only in touch mode. Detail panels need it too: a Material3 `Slider` stays focusable in touch mode, so the Article List panel scrolled down to its font size slider whenever it opened. The pane's request stops there, so nothing visible is focused. With a keyboard or D-pad the wrapper can't take focus, and focus moves into the list as usual. Tapping the search field still focuses it directly.

## Kept outside Settings

These are contextual shortcuts that mirror a registry setting:

- Per-feed "Open Articles In Browser" and the FreshRSS visibility in the feed edit dialog (per-feed, not global)
- Crash reporting on the pre-login screens (also in Advanced > Privacy)
- The reader style bottom sheet (also in Reader)
