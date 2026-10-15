# Play Review Account

Google Play reviewers sign in to a Feedbin test account to see the app with real content. Credentials live in Play Console under App access, not in this repo.

`fastlane/review/subscriptions.opml` tracks the account's feeds and folders so changes show up as diffs.

## Updating

1. Export subscriptions from the review account
2. `make review-opml OPML=~/Desktop/subscriptions.xml`
3. Commit `fastlane/review/subscriptions.opml`

The script drops the export date and the account email so re-exports only diff on feed changes.
