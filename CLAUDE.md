# CLAUDE.md

## Build and Development

- `./gradlew assembleFreeDebug` will compile the debug version of the app
- For fast feedback, run single tests i.e. `./gradlew :capy:test --tests com.jocmp.capy.persistence.ArticleRecordsTest` replacing the module - `:capy` - and Java package accordingly. Note `:capy` is a JVM module (use `:capy:test`); Android modules like `:app` use the variant task `testFreeDebugUnitTest`
- `make test` will run all tests via Gradle. Unit tests only run against the `free` flavor of `:app`; the `gplay` flavor shares the same test sources, so its unit test variant is disabled in `app/build.gradle.kts`.

## Project Architecture

Capy Reader is an RSS reader for Android split into several gradle modules

### Key Gradle Modules

- capy: Core application for account and feed management
- feedbinclient: Feedbin HTTP client
- readerclient: Google Reader API HTTP client
- feedfinder: Feed discovery helper
- rssparser: Feed parsing helper based on JSoup
- mallet: Flattens article HTML into a list of renderable elements for the native reader

### Key Architectural Patterns
- **Account System**: Pluggable account delegates for different sync services
- **Feed Management**: Hierarchical folder/feed organization with OPML import/export using SQLite
- **Article Rendering**: Template-based HTML rendering

## Code Style

- When naming accessors, prefer "savedSearches" over `getSavedSearches` unless there's a parameter, in which case use "get"
- Prefer explicit named parameters when passing arguments to Jetpack Compose functions over positional arguments.
- Prefer `orEmpty()` instead of `?: ""`
- Prefer functional iteration (map, forEach) as opposed to for-loops
