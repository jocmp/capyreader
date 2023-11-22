# Persistence

## 2023-11-20

NetNewsWire adds directory per-account which holds the following

```sh
## Account
tree Accounts/
Accounts/
└── OnMyMac
    ├── DB.sqlite3
    ├── FeedMetadata.plist
    ├── Settings.plist
    └── Subscriptions.opml
```

`DB.sqlite`
  - the service's article database

`FeedMetadata.plist`
  - maps each source to its last sync time, etag, author info, etc
  - Content Hash is also present for feed diffing ([NNW docs](https://github.com/Ranchero-Software/NetNewsWire/blob/e9f26c9adcbb87d0ff0fc8a7e0aacaf3f8ef5960/Technotes/AvoidFeedParsing.markdown))
  - Swift [source code](https://github.com/Ranchero-Software/NetNewsWire/blob/e1d2560fc0cac4e4de816149f998862bd89c052d/Account/Sources/Account/FeedMetadataFile.swift)

```jsonc
// FeedMetadata.plist converted to JSON
// ex `plutil -convert json -o FeedMetadata.json Accounts/OnMyMac/`
{
  "https:\/\/inessential.com\/feed.json": {
    "conditionalGetInfo": {
      "lastModified": "Tue, 27 Jun 2023 21:50:43 GMT",
      "etag": "\"13415-5ff23757392c0\""
    },
    "contentHash": "58510032283442d739c647362059952f",
    "feedID": "https:\/\/inessential.com\/feed.json",
    "authors": [
      {
        "avatarURL": "https:\/\/ranchero.com\/downloads\/brent_avatar.png",
        "name": "Brent Simmons",
        "url": "https:\/\/inessential.com\/",
        "authorID": "4e221eea34f82289b31b8ecf7ffedb6c"
      }
    ],
    "homePageURL": "https:\/\/inessential.com\/"
  }
}
```

`Settings.plist`
  - Very minimal file. Holds metadata as it relates to the account in settings

```jsonc
{
  "isActive": true,
  "performedApril2020RetentionPolicyChange": true, // feels... ultra-specific
  "lastArticleFetchEndTime": "2023-11-07T02:32:07Z"
}
```

`Subscriptions.opml`

The feeds! Folders are at most depth 1.

## 2023-11-06

- <https://developer.android.com/topic/libraries/architecture/datastore>
- <https://developer.android.com/training/data-storage/app-specific#internal-access-files>

  > The system prevents other apps from accessing these locations, and on Android 10 (API level 29) and higher, these locations are encrypted.
