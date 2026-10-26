package com.jocmp.mercury.utils.dom

// Spacer images to be removed
val SPACER_RE: Regex = Regex("transparent|spacer|blank", RegexOption.IGNORE_CASE)

// The class we will use to mark elements we want to keep
// but would normally remove
const val KEEP_CLASS: String = "mercury-parser-keep"

val KEEP_SELECTORS: List<String> =
    listOf(
        "iframe[src^=\"https://www.youtube.com\"]",
        "iframe[src^=\"https://www.youtube-nocookie.com\"]",
        "iframe[src^=\"http://www.youtube.com\"]",
        "iframe[src^=\"https://player.vimeo\"]",
        "iframe[src^=\"http://player.vimeo\"]",
        "iframe[src^=\"https://www.redditmedia.com\"]",
    )

// A list of tags to strip from the output if we encounter them.
val STRIP_OUTPUT_TAGS: List<String> =
    listOf(
        "title",
        "script",
        "noscript",
        "link",
        "style",
        "hr",
        "embed",
        "iframe",
        "object",
    )

// cleanAttributes
val REMOVE_ATTRS: List<String> = listOf("style", "align")
val REMOVE_ATTR_SELECTORS: List<String> = REMOVE_ATTRS.map { "[$it]" }
val REMOVE_ATTR_LIST: String = REMOVE_ATTRS.joinToString(",")
val WHITELIST_ATTRS: List<String> =
    listOf(
        "src",
        "srcset",
        "start",
        "sizes",
        "type",
        "href",
        "class",
        "id",
        "alt",
        "xlink:href",
        "width",
        "height",
    )

val WHITELIST_ATTRS_RE: Regex = Regex("^(${WHITELIST_ATTRS.joinToString("|")})$", RegexOption.IGNORE_CASE)

// removeEmpty
val REMOVE_EMPTY_TAGS: List<String> = listOf("p")
val REMOVE_EMPTY_SELECTORS: String = REMOVE_EMPTY_TAGS.joinToString(",") { "$it:empty" }

// cleanTags
val CLEAN_CONDITIONALLY_TAGS: String = listOf("ul", "ol", "table", "div", "button", "form").joinToString(",")

// cleanHeaders
private val HEADER_TAGS: List<String> = listOf("h2", "h3", "h4", "h5", "h6")
val HEADER_TAG_LIST: String = HEADER_TAGS.joinToString(",")

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
        // 'form', // This is too generic, has too many false positives
        "header",
        "hidden",
        "loader",
        // Note: This can hit 'blogindex'.
        "login",
        "menu",
        "meta",
        "nav",
        "outbrain",
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
        "taboola",
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

// A list of tags which, if found inside, should cause a <div /> to NOT
// be turned into a paragraph tag. Shallow div tags without these elements
// should be turned into <p /> tags.
val DIV_TO_P_BLOCK_TAGS: String = listOf("a", "blockquote", "dl", "div", "img", "p", "pre", "table").joinToString(",")

// A list of tags that should be ignored when trying to find the top candidate
// for a document.
val NON_TOP_CANDIDATE_TAGS: List<String> =
    listOf(
        "br", "b", "i", "label", "hr", "area", "base", "basefont",
        "input", "img", "link", "meta",
    )

val NON_TOP_CANDIDATE_TAGS_RE: Regex =
    Regex("^(${NON_TOP_CANDIDATE_TAGS.joinToString("|")})$", RegexOption.IGNORE_CASE)

// A list of selectors that specify, very clearly, either hNews or other
// very content-specific style content, like Blogger templates.
// More examples here: http://microformats.org/wiki/blog-post-formats
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

// A list of strings that denote a positive scoring for this content as being
// an article container. Checked against className and id.
//
// TODO: Perhaps have these scale based on their odds of being quality?
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

// The above list, joined into a matching regular expression
val POSITIVE_SCORE_RE: Regex = Regex(POSITIVE_SCORE_HINTS.joinToString("|"), RegexOption.IGNORE_CASE)

// Readability publisher-specific guidelines
val READABILITY_ASSET: Regex = Regex("entry-content-asset", RegexOption.IGNORE_CASE)

// A list of strings that denote a negative scoring for this content as being
// an article container. Checked against className and id.
//
// TODO: Perhaps have these scale based on their odds of being quality?
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

// The above list, joined into a matching regular expression
val NEGATIVE_SCORE_RE: Regex = Regex(NEGATIVE_SCORE_HINTS.joinToString("|"), RegexOption.IGNORE_CASE)

// XPath to try to determine if a page is wordpress. Not always successful.
const val IS_WP_SELECTOR: String = "meta[name=generator][value^=WordPress]"

// Match a digit. Pretty clear.
val DIGIT_RE: Regex = Regex("[0-9]")

// A list of words that, if found in link text or URLs, likely mean that
// this link is not a next page link.
val EXTRANEOUS_LINK_HINTS: List<String> =
    listOf(
        "print", "archive", "comment", "discuss", "e-mail", "email", "share",
        "reply", "all", "login", "sign", "single", "adx", "entry-unrelated",
    )
val EXTRANEOUS_LINK_HINTS_RE: Regex = Regex(EXTRANEOUS_LINK_HINTS.joinToString("|"), RegexOption.IGNORE_CASE)

// Match any phrase that looks like it could be page, or paging, or pagination
val PAGE_RE: Regex = Regex("pag(e|ing|inat)", RegexOption.IGNORE_CASE)

// Match any link text/classname/id that looks like it could mean the next
// page. Things like: next, continue, >, >>, » but not >|, »| as those can
// mean last page.
val NEXT_LINK_TEXT_RE: Regex = Regex("""(next|weiter|continue|>([^|]|$)|»([^|]|$))""", RegexOption.IGNORE_CASE)

// Match any link text/classname/id that looks like it is an end link: things
// like "first", "last", "end", etc.
val CAP_LINK_TEXT_RE: Regex = Regex("(first|last|end)", RegexOption.IGNORE_CASE)

// Match any link text/classname/id that looks like it means the previous
// page.
val PREV_LINK_TEXT_RE: Regex = Regex("(prev|earl|old|new|<|«)", RegexOption.IGNORE_CASE)

// Match 2 or more consecutive <br> tags
val BR_TAGS_RE: Regex = Regex("(<br[^>]*>[ \n\r\t]*){2,}", RegexOption.IGNORE_CASE)

// Match 1 BR tag.
val BR_TAG_RE: Regex = Regex("<br[^>]*>", RegexOption.IGNORE_CASE)

// A list of all of the block level tags known in HTML5 and below. Taken from
// http://bit.ly/qneNIT
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

// The removal is implemented as a blacklist and whitelist, this test finds
// blacklisted elements that aren't whitelisted. We do this all in one
// expression-both because it's only one pass, and because this skips the
// serialization for whitelisted nodes.
private val candidatesBlacklist: String = UNLIKELY_CANDIDATES_BLACKLIST.joinToString("|")
val CANDIDATES_BLACKLIST: Regex = Regex(candidatesBlacklist, RegexOption.IGNORE_CASE)

private val candidatesWhitelist: String = UNLIKELY_CANDIDATES_WHITELIST.joinToString("|")
val CANDIDATES_WHITELIST: Regex = Regex(candidatesWhitelist, RegexOption.IGNORE_CASE)

val UNLIKELY_RE: Regex = Regex("!($candidatesWhitelist)|($candidatesBlacklist)", RegexOption.IGNORE_CASE)

val PARAGRAPH_SCORE_TAGS: Regex = Regex("^(p|li|span|pre)$", RegexOption.IGNORE_CASE)
val CHILD_CONTENT_TAGS: Regex = Regex("^(td|blockquote|ol|ul|dl)$", RegexOption.IGNORE_CASE)
val BAD_TAGS: Regex = Regex("^(address|form)$", RegexOption.IGNORE_CASE)

val HTML_OR_BODY_RE: Regex = Regex("^(html|body)$", RegexOption.IGNORE_CASE)
