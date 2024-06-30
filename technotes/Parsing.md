# Feed Finder

1. XMLFeed
2. JSONFeed (wontdo)
3. HTML
  a. Meta Links
  b. Body Links
4. XMLFeedGuess (second pass)

- XML can be parsed directly if XML feed
- HTML takes response body

## Reading from File

1. Parse from OPML
2. Find all feeds based on IDs
3. Map feed from DB to set

## Timestamps

The `pubDate` field may not be a ISO 8601 format instead using the following format

```
D, d M Y H:i:s O
```

Example:

- Sat, 29 Jun 2024 10:55:08 +0000

