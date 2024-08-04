# Capy Reader

<img src="./site/capy.png" width="100px">

_A smallish RSS reader with support for Feedbin and local feeds._

![GitHub Release][github_img] ![Tests](https://github.com/jocmp/capyreader/actions/workflows/ci.yml/badge.svg)

## Download

- [Google Play][gplay_link]
- [F-Droid][fdroid_link]
- [IzzyOnDroid][izzy_link]
- [GitHub Releases][github_link]

Capy Reader is free across all sources.

### Releases

Google Play releases happen on a weekly basis give or take Google's time to review.

GitHub releases are marked with a "-dev" suffix and are builds that haven't been submitted
to Google Play yet. They are always stable builds but are released more frequently.

## Reporting a bug

Bug reports are always welcome and are my top priority.

If you run into an issue, let me know by submitting a [bug report](https://github.com/jocmp/capyreader/issues/new?labels=bug&template=bug_report.yml).

## Feature Requests

If you have questions or feature requests, please post them to [Discussions](https://github.com/jocmp/capyreader/discussions).

## Building the app

### Getting Started

1. Clone this repository
2. Install [Android Studio](https://developer.android.com/studio) if you don't have it already
3. Sync Gradle
4. In the toolbar, go to Run > Run 'app'

#### Build a signed release (Optional)

By default the app will build with a debug keystore. Follow the instructions below to build a signed release.

1. Ensure you have a keystore with the name `release.keystore` in the root directory.
2. Next, create a file called `secrets.properties`, also in the root directory, with the following values

    ```properties
    key_alias=
    store_password=
    key_password=
    ```


[gplay_link]: https://play.google.com/store/apps/details?id=com.capyreader.app
[fdroid_link]: https://f-droid.org/packages/com.capyreader.app/
[izzy_link]: https://apt.izzysoft.de/fdroid/index/apk/com.capyreader.app
[izzy_img]: https://img.shields.io/endpoint?url=https://apt.izzysoft.de/fdroid/api/v1/shield/com.capyreader.app&label=IzzyOnDroid
[github_link]: https://github.com/jocmp/capyreader/releases/latest
[github_img]: https://img.shields.io/github/v/release/jocmp/capyreader?logo=GitHub
