# CLAUDE.md

## Build and Development

- `./gradlew assembleFreeDebug` will compile the debug version of the app
- For fast feedback, run single tests i.e. `./gradlew :capy:testDebugUnitTest --tests com.jocmp.capy.persistence.ArticleRecordsTest` replacing the module - `:capy` - and Java package accordingly
- `make test` will run all tests via Fastlane.
- When modifying the `.js` and `.liquid` files, be sure to run `make` to compile those assets, and `make check` to typecheck

## Project Architecture

Capy Reader is an RSS reader for Android split into several gradle modules

### Key Gradle Modules

- capy: Core application for account and feed management
- feedbinclient: Feedbin HTTP client
- readerclient: Google Reader API HTTP client
- feedfinder: Feed discovery helper
- rssparser: Feed parsing helper based on JSoup

### Key Architectural Patterns
- **Account System**: Pluggable account delegates for different sync services
- **Feed Management**: Hierarchical folder/feed organization with OPML import/export using SQLite
- **Article Rendering**: Template-based HTML rendering

## Code Style

- When naming accessors, prefer "savedSearches" over `getSavedSearches` unless there's a parameter, in which case use "get"
- Prefer explicit named parameters when passing arguments to Jetpack Compose functions over positional arguments.
- JavaScript files are written using JSDoc to ensure typechecking without the overhead of TypeScript.
- Prefer `orEmpty()` instead of `?: ""`
