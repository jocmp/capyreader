# Models

A folder has many feeds

A feed may exist outside of a folder. These are listed last after all folders.

## Feed lookup

* Look up all subscriptions
* Look up all taggings
* On success, query subscriptions and join on taggings
* Group subscriptions by their taggings
* Return a list of folders and subscriptions

Given a subscription that has a tagging and a subscription without a tagging:

9to5google.com tagged by Tech
blockclubchicago.com without a tagging

In memory
```
[
  Folder("Tech", feeds = [Feed("9to5google.com")]),
  Feed("blockclubchicago.com")
]
```

In the UI
```
Tech
  * 9to5google.com
blockclubchicago.com
```
