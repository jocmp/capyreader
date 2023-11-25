`/accounts`
AccountManagerView: Single page. Handles account creation and deletion

`/folder/{folder_id}`
ArticleView: List/detail.

- Let folder_id be the integer ID from Feedbin
- Shows one feed and currently selected article if present
- Subscriptions will be shown in drawer. Filters at bottom or side-rail(?)

Feedbin separates these concepts into routes

| Action               | URL                                                                            |
| -------------------- | ------------------------------------------------------------------------------ |
| Viewing a Folder/Tag | <https://feedbin.com/tags/62?view_mode=view_unread>                            |
| Viewing a feed       | <https://feedbin.com/feeds/2599689/entries?view_mode=view_unread>              |
| Viewing all unread   | <https://feedbin.com/entries/unread?view_mode=view_unread>                     |
| Viewing an article   | Call https://feedbin.com/entries/4275542622/mark_as_read, pivot to detail view |

| Basil Action         | Route               | Notes                                         |
| -------------------- | ------------------- | --------------------------------------------- |
| Viewing a Folder/Tag | /folder/{folder_id} |                                               |
| Viewing a feed       | /feeds/{feed_id}    |                                               |
| Viewing all unread   | /articles/{filter}  | Where filter is `read`, `unread` or `starred` |

