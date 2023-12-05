package com.jocmp.feedfinder.sources

//            val urls = listOf(parsedURL) + variations.map { variation ->
//                parsedURL.resolve(variation)
//            }
//
//            val documents = coroutineScope {
//                urls.map { async { fetchDocument(it) } }
//                    .awaitAll()
//                    .filterNotNull()
//            }

//            return find(documents = documents)

private val variations = listOf(
    "feed",
    "rss"
)
