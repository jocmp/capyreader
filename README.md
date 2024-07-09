# Capy Reader

<img src="./site/capy.png" width="100px">

[![CalVer 2024.07.1003][img_version]][url_version] ![Continuous Integration](https://github.com/jocmp/capyreader/actions/workflows/ci.yml/badge.svg)

_A smallish RSS reader with support for Feedbin and local feeds._

<!-- [Download on Google Play][google_play_link] -->

## Reporting a bug

If you run into a bug, let me know by submitting a [bug report](https://github.com/jocmp/capyreader/issues/new?labels=bug&template=bug_report.yml)

## Getting Started

1. Clone this repository
2. Install [Android Studio](https://developer.android.com/studio) if you don't have it already
3. Sync Gradle
4. In the toolbar, go to Run > Run 'app'

### Build a signed release (Optional)

By default the app will build with a debug keystore. Follow the instructions below to build a signed release.

1. Ensure you have a keystore with the name `release.keystore` in the root directory.
2. Next, create a file called `secrets.properties`, also in the root directory, with the following values
    ```properties
    key_alias=
    store_password=
    key_password=
    ```


[img_version]: https://img.shields.io/static/v1.svg?label=CalVer&message=2024.07.1003&color=blue
[url_version]: https://github.com/jocmp/capyreader
[google_play_link]: https://example.com
