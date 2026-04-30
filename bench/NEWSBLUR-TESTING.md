# NewsBlur bench testing

Phase 1 of the NewsBlur integration ships only the `login` flow. Use this
document to verify that credentials work against a live NewsBlur account
before Phase 2 lands the rest of the sync surface.

## Setup

1. Copy the example config into place and fill it in:

   ```sh
   cp bench/bench.properties.newsblur.example bench/bench.properties
   ```

   `bench/bench.properties` is gitignored.

2. Edit `bench/bench.properties`:

   ```properties
   source=newsblur
   username=YOUR_NEWSBLUR_USERNAME
   password=YOUR_NEWSBLUR_PASSWORD
   url=https://newsblur.com/
   ```

   Leave `url` blank to default to `https://newsblur.com/`.

## Run login

```sh
./gradlew :bench:run --args="login"
```

Expected output on success:

```
Authenticated as <your_username>
```

On a bad password you should see:

```
Login failed: <message from NewsBlur>
```

The `login` command never writes to `bench/data/` — it only verifies the
session against `POST /api/login`.

## Phase 2 commands (deferred)

The following bench flows depend on delegate methods that currently throw
`NotImplementedError("NewsBlur Phase 2")`. They will land in a follow-up
PR alongside the corresponding delegate work:

- `refresh` — needs `refresh()`, `fetchFolders`, `fetchFeeds`, `fetchUnreadHashes`, story content fetch.
- `feeds` — depends on a successful `refresh` populating the feeds table.
- `articles` — depends on a successful `refresh` populating articles.
- `add-feed` — needs `addFeed` (NewsBlur `reader/add_url`).
- `mark-read` / `mark-unread` / `mark-starred` — need `markRead`, `markUnread`, `addStar`, `removeStar`.

Run those commands today and they will throw immediately — that's the
contract Phase 2 will fulfill.
