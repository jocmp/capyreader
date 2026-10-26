package com.jocmp.mercury.extractors.generic.content.scoring

// // CONTENT FETCHING CONSTANTS ////

// A list of strings that can be considered unlikely candidates when
// extracting content from a resource. These strings are joined together
// and then tested for existence using re:test, so may contain simple,
// non-pipe style regular expression queries if necessary.
val UNLIKELY_CANDIDATES_BLACKLIST: List<String> =
    listOf(
        "ad-break",
        "adbox",
        "advert",
        "addthis",
        "agegate",
        "aux",
        "blogger-labels",
        "combx",
        "comment",
        "conversation",
        "disqus",
        "entry-unrelated",
        "extra",
        "foot",
        "form",
        "header",
        "hidden",
        "loader",
        // Note: This can hit 'blogindex'.
        "login",
        "menu",
        "meta",
        "nav",
        "pager",
        "pagination",
        // readwriteweb inline ad box
        "predicta",
        // lifehacker.com container full of false positives
        "presence_control_external",
        "popup",
        "printfriendly",
        "related",
        "remove",
        "remark",
        "rss",
        "share",
        "shoutbox",
        "sidebar",
        "sociable",
        "sponsor",
        "tools",
    )

// A list of strings that can be considered LIKELY candidates when
// extracting content from a resource. Essentially, the inverse of the
// blacklist above - if something matches both blacklist and whitelist,
// it is kept. This is useful, for example, if something has a className
// of "rss-content entry-content". It matched 'rss', so it would normally
// be removed, however, it's also the entry content, so it should be left
// alone.
//
// These strings are joined together and then tested for existence using
// re:test, so may contain simple, non-pipe style regular expression queries
// if necessary.
val UNLIKELY_CANDIDATES_WHITELIST: List<String> =
    listOf(
        "and",
        "article",
        "body",
        "blogindex",
        "column",
        "content",
        "entry-content-asset",
        // misuse of form
        "format",
        "hfeed",
        "hentry",
        "hatom",
        "main",
        "page",
        "posts",
        "shadow",
    )

val DIV_TO_P_BLOCK_TAGS: String = listOf("a", "blockquote", "dl", "div", "img", "p", "pre", "table").joinToString(",")

val NON_TOP_CANDIDATE_TAGS: List<String> =
    listOf(
        "br", "b", "i", "label", "hr", "area", "base", "basefont",
        "input", "img", "link", "meta",
    )

val NON_TOP_CANDIDATE_TAGS_RE: Regex =
    Regex("^(${NON_TOP_CANDIDATE_TAGS.joinToString("|")})$", RegexOption.IGNORE_CASE)

val HNEWS_CONTENT_SELECTORS: List<Pair<String, String>> =
    listOf(
        ".hentry" to ".entry-content",
        "entry" to ".entry-content",
        ".entry" to ".entry_content",
        ".post" to ".postbody",
        ".post" to ".post_body",
        ".post" to ".post-body",
    )

val PHOTO_HINTS: List<String> = listOf("figure", "photo", "image", "caption")
val PHOTO_HINTS_RE: Regex = Regex(PHOTO_HINTS.joinToString("|"), RegexOption.IGNORE_CASE)

val POSITIVE_SCORE_HINTS: List<String> =
    listOf(
        "article",
        "articlecontent",
        "instapaper_body",
        "blog",
        "body",
        "content",
        "entry-content-asset",
        "entry",
        "hentry",
        "main",
        "Normal",
        "page",
        "pagination",
        "permalink",
        "post",
        "story",
        "text",
        // usatoday
        "[-_]copy",
        "\\Bcopy",
    )

val POSITIVE_SCORE_RE: Regex = Regex(POSITIVE_SCORE_HINTS.joinToString("|"), RegexOption.IGNORE_CASE)

val READABILITY_ASSET: Regex = Regex("entry-content-asset", RegexOption.IGNORE_CASE)

val NEGATIVE_SCORE_HINTS: List<String> =
    listOf(
        "adbox", "advert", "author", "bio", "bookmark", "bottom", "byline",
        "clear", "com-", "combx", "comment", "comment\\B", "contact", "copy",
        "credit", "crumb", "date", "deck", "excerpt",
        // tnr.com has a featured_content which throws us off
        "featured",
        "foot", "footer", "footnote", "graf", "head", "info",
        // newscientist.com copyright
        "infotext",
        "instapaper_ignore", "jump", "linebreak", "link", "masthead", "media",
        "meta", "modal",
        // slate.com junk
        "outbrain",
        "promo",
        // autoblog - press release
        "pr_",
        "related", "respond",
        // lifehacker restricted content warning
        "roundcontent",
        "scroll", "secondary", "share", "shopping", "shoutbox", "side", "sidebar",
        "sponsor", "stamp", "sub", "summary", "tags", "tools", "widget",
    )
val NEGATIVE_SCORE_RE: Regex = Regex(NEGATIVE_SCORE_HINTS.joinToString("|"), RegexOption.IGNORE_CASE)

val DIGIT_RE: Regex = Regex("[0-9]")

val BR_TAGS_RE: Regex = Regex("(<br[^>]*>[ \n\r\t]*){2,}", RegexOption.IGNORE_CASE)
val BR_TAG_RE: Regex = Regex("<br[^>]*>", RegexOption.IGNORE_CASE)

val BLOCK_LEVEL_TAGS: List<String> =
    listOf(
        "article", "aside", "blockquote", "body", "br", "button", "canvas",
        "caption", "col", "colgroup", "dd", "div", "dl", "dt", "embed",
        "fieldset", "figcaption", "figure", "footer", "form", "h1", "h2",
        "h3", "h4", "h5", "h6", "header", "hgroup", "hr", "li", "map",
        "object", "ol", "output", "p", "pre", "progress", "section", "table",
        "tbody", "textarea", "tfoot", "th", "thead", "tr", "ul", "video",
    )
val BLOCK_LEVEL_TAGS_RE: Regex = Regex("^(${BLOCK_LEVEL_TAGS.joinToString("|")})$", RegexOption.IGNORE_CASE)

private val candidatesBlacklist: String = UNLIKELY_CANDIDATES_BLACKLIST.joinToString("|")
val CANDIDATES_BLACKLIST: Regex = Regex(candidatesBlacklist, RegexOption.IGNORE_CASE)

private val candidatesWhitelist: String = UNLIKELY_CANDIDATES_WHITELIST.joinToString("|")
val CANDIDATES_WHITELIST: Regex = Regex(candidatesWhitelist, RegexOption.IGNORE_CASE)

val UNLIKELY_RE: Regex = Regex("!($candidatesWhitelist)|($candidatesBlacklist)", RegexOption.IGNORE_CASE)

val PARAGRAPH_SCORE_TAGS: Regex = Regex("^(p|li|span|pre)$", RegexOption.IGNORE_CASE)
val CHILD_CONTENT_TAGS: Regex = Regex("^(td|blockquote|ol|ul|dl)$", RegexOption.IGNORE_CASE)
val BAD_TAGS: Regex = Regex("^(address|form)$", RegexOption.IGNORE_CASE)

val HTML_OR_BODY_RE: Regex = Regex("^(html|body)$", RegexOption.IGNORE_CASE)
