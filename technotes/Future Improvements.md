# Future Improvements

## Database Schema

### Add ON DELETE CASCADE to foreign keys

The following tables reference `articles(id)` but lack `ON DELETE CASCADE`:

- `saved_search_articles.article_id`
- `enclosures.article_id`
- `article_notifications.article_id` (currently has no foreign key constraint)

Adding `ON DELETE CASCADE` would simplify article deletion queries by automatically cleaning up related records when articles are deleted. This would require a migration to recreate these tables since SQLite doesn't support `ALTER TABLE` to modify constraints.
