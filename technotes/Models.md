## Models

- Account
- SubscriptionList
  - OPML File
  - Has many folders
  - Has many subscriptions
- Folder
  - Has many subscriptions
  - Belongs to Subscription List
- Subscription
  - Edit Subscription
  - Has-a feed
  - Belongs to Subscription List or a Folder
- Feed
- Article
  - Data
    - ID
    - Belongs to Feed
    - Has-a status
- ArticleStatus
  - Behavior
  - Data
    - Article ID (Unique within Account)
    - Belongs to an Article
    - Read
    - Starred
    - Arrived At (First seen, used for cache eviction)


## Persistence

- <https://developer.android.com/topic/libraries/architecture/datastore>
- <https://developer.android.com/training/data-storage/app-specific#internal-access-files>

  > The system prevents other apps from accessing these locations, and on Android 10 (API level 29) and higher, these locations are encrypted.

# Packages

## basilreader

- UI
- Background updates

## basil

- Authentication
- Persistence
- Source interface

## feedbin

Vendor specific. Only responsible for communicating with Feedbin.
