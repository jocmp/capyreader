# Sync

On boot or manual refresh action, refresh the Feedbin account via the network.

From NetNewsWire's [FeedbinAccountDelegate]

```swift
func refreshAccount(_ account: Account, completion: @escaping (Result<Void, Error>) -> Void) {
  caller.retrieveTags { result in
    switch result {
    case .success(let tags):

      self.refreshProgress.completeTask()
      self.caller.retrieveSubscriptions { result in
        switch result {
        case .success(let subscriptions):

          self.refreshProgress.completeTask()
          self.forceExpireFolderFeedRelationship(account, tags) // 1. Remove missing or changed tags
          self.caller.retrieveTaggings { result in
            switch result {
            case .success(let taggings):

              BatchUpdate.shared.perform {
                self.syncFolders(account, tags) // 2. Sync folders
                self.syncFeeds(account, subscriptions) // 3. Sync feeds
                self.syncFeedFolderRelationship(account, taggings) // 4. Sync
              }

              self.refreshProgress.completeTask()
              completion(.success(()))

            case .failure(let error):
              completion(.failure(error))
            }

          }

        case .failure(let error):
          completion(.failure(error))
        }

      }

    case .failure(let error):
      completion(.failure(error))
    }
  }
}
```

### 1. Remove missing or changed tags

```swift
// This function can be deleted if Feedbin updates their taggings.json service to
// show a change when a tag is renamed.
func forceExpireFolderFeedRelationship(_ account: Account, _ tags: [FeedbinTag]?) {
  guard let tags = tags else { return }

  let folderNames: [String] =  {
    if let folders = account.folders {
      return folders.map { $0.name ?? "" }
    } else {
      return [String]()
    }
  }()

  // Feedbin has a tag that we don't have a folder for.  We might not get a new
  // taggings response for it if it is a folder rename.  Force expire the tagging
  // so that we will for sure get the new tagging information.
      for tag in tags {
    if !folderNames.contains(tag.name) {
      accountMetadata?.conditionalGetInfo[FeedbinAPICaller.ConditionalGetKeys.taggings] = nil
    }
  }
}
```

### 2. Sync Folders

```swift
func syncFolders(_ account: Account, _ tags: [FeedbinTag]?) {
  guard let tags = tags else { return }
  assert(Thread.isMainThread)

      logger.debug("Syncing folders with \(tags.count, privacy: .public) tags.")

  let tagNames = tags.map { $0.name }

  // Delete any folders not at Feedbin
  if let folders = account.folders {
          for folder in folders {
      if !tagNames.contains(folder.name ?? "") {
        for feed in folder.topLevelFeeds {
          account.addFeed(feed)
          clearFolderRelationship(for: feed, withFolderName: folder.name ?? "")
        }
        account.removeFolder(folder)
      }
    }
  }

  let folderNames: [String] =  {
    if let folders = account.folders {
      return folders.map { $0.name ?? "" }
    } else {
      return [String]()
    }
  }()

  // Make any folders Feedbin has, but we don't
      for tagName in tagNames {
    if !folderNames.contains(tagName) {
      _ = account.ensureFolder(with: tagName)
    }
  }

}
```

### 3. Sync feeds

```swift
func syncFeeds(_ account: Account, _ subscriptions: [FeedbinSubscription]?) {
  guard let subscriptions = subscriptions else { return }
  assert(Thread.isMainThread)

      logger.debug("Syncing feeds with \(subscriptions.count, privacy: .public) subscriptions.")

      let subFeedIDs = subscriptions.map { String($0.feedID) }

      // Remove any feeds that are no longer in the subscriptions
      if let folders = account.folders {
          for folder in folders {
              for feed in folder.topLevelFeeds {
                  if !subFeedIDs.contains(feed.feedID) {
                      folder.removeFeed(feed)
                  }
              }
          }
      }

      for feed in account.topLevelFeeds {
          if !subFeedIDs.contains(feed.feedID) {
              account.removeFeed(feed)
          }
      }

      // Add any feeds we don't have and update any we do
      var subscriptionsToAdd = Set<FeedbinSubscription>()
      for subscription in subscriptions {

    let subFeedID = String(subscription.feedID)

    if let feed = account.existingFeed(withFeedID: subFeedID) {
      feed.name = subscription.name
      // If the name has been changed on the server remove the locally edited name
      feed.editedName = nil
      feed.homePageURL = subscription.homePageURL
      feed.externalID = String(subscription.subscriptionID)
      feed.faviconURL = subscription.jsonFeed?.favicon
      feed.iconURL = subscription.jsonFeed?.icon
    }
    else {
      subscriptionsToAdd.insert(subscription)
    }
  }

  // Actually add subscriptions all in one go, so we don’t trigger various rebuilding things that Account does.
      for subscription in subscriptionsToAdd {
    let feed = account.createFeed(with: subscription.name, url: subscription.url, feedID: String(subscription.feedID), homePageURL: subscription.homePageURL)
    feed.externalID = String(subscription.subscriptionID)
    account.addFeed(feed)
  }
}
```

### 4. Sync folders to feeds

```swift
func syncFeedFolderRelationship(_ account: Account, _ taggings: [FeedbinTagging]?) {
  guard let taggings = taggings else { return }
  assert(Thread.isMainThread)

      logger.debug("Syncing taggings with \(taggings.count, privacy: .public) taggings.")

  // Set up some structures to make syncing easier
  let folderDict = nameToFolderDictionary(with: account.folders)
  let taggingsDict = taggings.reduce([String: [FeedbinTagging]]()) { (dict, tagging) in
    var taggedFeeds = dict
    if var taggedFeed = taggedFeeds[tagging.name] {
      taggedFeed.append(tagging)
      taggedFeeds[tagging.name] = taggedFeed
    } else {
      taggedFeeds[tagging.name] = [tagging]
    }
    return taggedFeeds
  }

  // Sync the folders
  for (folderName, groupedTaggings) in taggingsDict {

    guard let folder = folderDict[folderName] else { return }

    let taggingFeedIDs = groupedTaggings.map { String($0.feedID) }

    // Move any feeds not in the folder to the account
    for feed in folder.topLevelFeeds {
      if !taggingFeedIDs.contains(feed.feedID) {
        folder.removeFeed(feed)
        clearFolderRelationship(for: feed, withFolderName: folder.name ?? "")
        account.addFeed(feed)
      }
    }

    // Add any feeds not in the folder
    let folderFeedIDs = folder.topLevelFeeds.map { $0.feedID }

    for tagging in groupedTaggings {
      let taggingFeedID = String(tagging.feedID)
      if !folderFeedIDs.contains(taggingFeedID) {
        guard let feed = account.existingFeed(withFeedID: taggingFeedID) else {
          continue
        }
        saveFolderRelationship(for: feed, withFolderName: folderName, id: String(tagging.taggingID))
        folder.addFeed(feed)
      }
    }

  }

  let taggedFeedIDs = Set(taggings.map { String($0.feedID) })

  // Remove all feeds from the account container that have a tag
  for feed in account.topLevelFeeds {
    if taggedFeedIDs.contains(feed.feedID) {
      account.removeFeed(feed)
    }
  }
}
```

[FeedbinAccountDelegate]: https://github.com/Ranchero-Software/NetNewsWire/blob/6cd8715eb08252a7332db55b8c9b8c657485d8ce/Account/Sources/Account/AccountDelegates/FeedbinAccountDelegate.swift#L726-L768
