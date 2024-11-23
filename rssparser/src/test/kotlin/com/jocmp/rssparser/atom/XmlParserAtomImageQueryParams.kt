package com.jocmp.rssparser.atom

import com.jocmp.rssparser.BaseParserTest
import com.jocmp.rssparser.model.RssImage

class XmlParserAtomImageQueryParams : BaseParserTest(
    feedPath = "atom-image-query-params.xml",
    channelTitle = "9to5Google",
    channelLink = "https://9to5google.com/",
    channelLastBuildDate = "Mon, 02 Sep 2024 17:32:19 +0000",
    channelUpdatePeriod = "hourly",
    channelDescription = "Google news, Pixel, Android, Home, Chrome OS, more",
    channelImage = RssImage(
        title = "9to5Google",
        url = "https://9to5google.com/wp-content/client-mu-plugins/9to5-core/includes/obfuscate-images/images/9to5google-default.jpg?quality=82&strip=all&w=32",
        link = "https://9to5google.com/",
        description = null
    ),
    articleTitle = "Labor Day Android game and app price drops: SOULVARS, Bloons TD 6, Space Grunts 2, more",
    articlePubDate = "Mon, 02 Sep 2024 17:32:19 +0000",
    articleLink = "https://9to5toys.com/2024/09/02/labor-day-android-game-app-deals/",
    articleGuid = "https://9to5google.com/2024/09/02/labor-day-android-game-app-deals/",
    articleImage = "https://9to5google.com/wp-content/uploads/sites/4/2024/09/SOULVARS.jpg?quality=82&strip=all&w=1600",
    articleCommentsUrl = "https://9to5toys.com/2024/09/02/labor-day-android-game-app-deals/#respond",
    articleAuthor = "9to5Toys",
    articleDescription = """
    <div class="feat-image"><img src="https://9to5google.com/wp-content/uploads/sites/4/2024/09/SOULVARS.jpg?quality=82&#038;strip=all&#038;w=1600" /></div><p>Your Labor Day edition of the best Android game and app price drops is now ready to go down below. Our <a href="https://9to5toys.com/2024/08/30/best-labor-day-deals-2/">master collection</a> of all the best Labor Day deals is waiting for you <a href="https://9to5toys.com/2024/08/30/best-labor-day-deals-2/">right here</a>, highlighted by <a href="https://9to5toys.com/2024/09/02/samsung-unlocked-galaxy-s24-ultra-labor-day/"><strong>${'$'}250 </strong>price drops on Samsung Galaxy S24 Ultra</a> handsets alongside the best price ever on <a href="https://9to5toys.com/2024/09/02/best-price-ever-hits-samsung-galaxy-book-4-edge-copilot-pc-at-900/">Samsung’s new Snapdragon X Elite Galaxy Book 4 Edge Copilot+ PC</a>. Just be sure to also check out our roundup of the <a href="https://9to5toys.com/2024/08/29/best-labor-day-deals-under-30/">best Labor Day deals under <strong>${'$'}30</strong></a> and our <a href="https://9to5toys.com/2024/09/02/top-10-labor-day-deals-2024/">top 10 favorite Labor Day deals</a> as well. As for the apps, highlights include titles like SOULVARS, Bloons TD 6, Streets of Rage 4, Space Grunts 2, Mazetools Mutant, and more. Head below for a closer look. </p>



     <a data-layer-pagetype="post" data-layer-postcategory="" data-layer-viewtype="unknown" data-post-id="638956" href="https://9to5toys.com/2024/09/02/labor-day-android-game-app-deals/#more-638956" class="more-link">more…</a>
    """.trimIndent(),
    articleCategories = listOf("Deals")
)
