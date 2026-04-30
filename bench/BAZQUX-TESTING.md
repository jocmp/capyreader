# BazQux live verification

These steps exercise a real BazQux Reader account against the bench CLI.
The bench module wires the same `ReaderAccountDelegate` the Android app
uses, so a green run here implies the Android client is also wired up.

## 1. Drop credentials

```sh
cp bench/bench.properties.bazqux.example bench/bench.properties
$EDITOR bench/bench.properties   # fill in username + password
```

`bench/bench.properties` is gitignored. Confirm with `git check-ignore -v
bench/bench.properties` if you want to be paranoid.

## 2. Verify credentials

```sh
./gradlew :bench:run --args="login"
```

Expect: `Logged in as <username>` and exit 0. Bad credentials return exit
1 with a single-line error (no stack trace). On first run this also
creates the local on-disk account under `bench/data/`.

## 3. First sync

```sh
./gradlew :bench:run --args="refresh"
```

Expect a feed count and an article count. Re-running should be cheaper
and idempotent.

## 4. Inspect

```sh
./gradlew :bench:run --args="folders"
./gradlew :bench:run --args="feeds"
./gradlew :bench:run --args="articles"
./gradlew :bench:run --args="articles unread"
./gradlew :bench:run --args="articles starred"
```

`articles <status>` prints the first 50 articles with a `[rs]` flag
column (read, starred).

## 5. Round-trip read state

Pick an article ID from `articles unread` output, then:

```sh
./gradlew :bench:run --args="mark-read <article-id>"
./gradlew :bench:run --args="refresh"
./gradlew :bench:run --args="articles unread"
```

The article should no longer appear in the unread list. Re-run
`mark-read <article-id>` to flip it back to unread and verify it
returns.

## 6. Round-trip starred state

```sh
./gradlew :bench:run --args="mark-starred <article-id>"
./gradlew :bench:run --args="refresh"
./gradlew :bench:run --args="articles starred"
```

Same idea — the article should appear in starred. Re-run to unstar.

## 7. Reset

```sh
./gradlew :bench:run --args="reset"
```

Wipes `bench/data/` so the next run logs in afresh.

## Reporting results

Comment on the BazQux PR with:

- Output of `login`, `refresh`, `articles`, `folders`.
- Whether the read/starred round-trips succeeded.
- Anything weird (timestamps off, folders missing, item IDs that look
  like negative 64-bit integers but get truncated, etc).
