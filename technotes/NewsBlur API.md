# NewsBlur API

Docs: <https://www.newsblur.com/api>. Server source: <https://github.com/samuelclay/NewsBlur>, mostly `apps/reader/views.py` and `apps/reader/models.py`.

No API key. Auth is a Django session: `POST api/login` with `username`/`password` sets a `newsblur_sessionid` cookie. Capy stores the session ID as the account secret (like the Reader ClientLogin token) and sends `Cookie: newsblur_sessionid=...` on every request. OAuth requires developer credentials from NewsBlur, so it is not used.

Self-hosted instances use the same API. A blank URL on the login screen means `https://www.newsblur.com/`.

## Sync

Same shape as NetNewsWire's NewsBlur account:

1. `GET reader/feeds?flat=true` -> feeds plus `flat_folders`
2. `GET reader/unread_story_hashes` -> `markAllUnread`
3. `GET reader/starred_story_hashes` -> `markAllStarred`
4. `findMissingArticles` -> `GET reader/river_stories?h=...&h=...`, 100 hashes per request

Story hashes (`<feed_id>:<guid_hash>`) are the article IDs. `read_status` is `1` when read; `starred` is absent unless the story is starred.

Only unread and starred stories are fetched. NewsBlur's unread window is 15 days on free accounts and 30 on premium, so older unread stories never arrive.

## Free accounts

The API works on a free account, with limits:

- 64 feeds
- `river_stories` paged by feed or folder returns at most 3 stories and blocks page 2+. Fetching by hash (`h=`) is not limited, which is why sync goes through hashes.
- Responses on the hash path still say `"code": 0, "message": "The full River of News is a premium feature."`. Do not treat `code: 0` as a failure.
- Search is premium-only.

## Authentication failures

An expired or missing session does not always produce an error status:

| Endpoint | No session |
|---|---|
| `reader/feeds` | 403 |
| write endpoints (`mark_*`, `add_url`, ...) | 403 |
| `reader/unread_story_hashes` | 200 with `"authenticated": false` and a demo user's hashes |
| `reader/starred_story_hashes` | 200 with `"authenticated": false` |
| `reader/river_stories` | 200 with `"authenticated": false` |

The delegate treats 401/403 and `authenticated: false` on the hash endpoints as `UnauthorizedError`, so a demo user's stories are never saved.

## Folders

- `flat_folders` uses `" "` for top level and `"Parent - Child"` for nested folders. Folder writes use `""` for top level.
- A feed can sit in several folders. `delete_feed` removes one placement per call and unsubscribes only when the last one is gone, so `removeFeed` calls it once per folder.
- Moving or adding a feed into a folder that does not exist silently drops the feed from the folder tree. `add_folder` is idempotent, so the delegate calls it before `add_url` and `move_feed_to_folders`.
- `delete_folder` unsubscribes from the feed IDs passed with it and emails the user an OPML backup. `removeFolder` first moves the folder's feeds out (to top level, or nowhere if they're in another folder), then deletes the empty folder.
- Nested folders are shown by their flat name. Renaming or deleting a nested folder by that name does not match on the server.

## Status writes

`mark_story_hashes_as_read`, `mark_story_hash_as_unread`, `mark_story_hash_as_starred` and `mark_story_hash_as_unstarred` all accept repeated `story_hash` fields. Capy sends 50 per request.
