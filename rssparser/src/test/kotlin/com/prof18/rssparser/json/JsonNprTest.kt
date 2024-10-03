package com.prof18.rssparser.json

import com.prof18.rssparser.BaseParserTest
import com.prof18.rssparser.model.RssImage

class JsonNprTest : BaseParserTest(
    feedPath = "feed-npr-world.json",
    channelTitle = "World",
    channelLink = "https://www.npr.org/sections/world/?utm_medium=JSONFeed&utm_campaign=world",
    channelDescription = "NPR world news, international art and culture, world business and financial markets, world economy, and global trends in health, science and technology. Subscribe to the World Story of the Day podcast and RSS feed.",
    channelImage = RssImage(
        title = null,
        link = null,
        description = null,
        url = "https://media.npr.org/images/stations/nprone_logos/npr.png"
    ),
    articleTitle = "Eggs and Bananas: Life after a Russian prison",
    articleLink = "https://www.npr.org/2024/08/26/1198913145/eggs-and-bananas-life-after-a-russian-prison?utm_medium=JSONFeed&utm_campaign=world",
    articleDescription = """
        It's been more than three weeks since the U.S. and Russia completed the largest prisoner swap since the collapse of the Soviet Union.Speaking from the White House shortly after news broke that three American prisoners were headed home, President Biden described the release as an "incredible relief."Russian-American journalist Alsu Kurmasheva was one of those prisoners, and she's sharing what life was like in a Russian prison and how she's adjusting to life at home. For sponsor-free episodes of Consider This, sign up for Consider This+ via Apple Podcasts or at plus.npr.org.Email us at considerthis@npr.org.
    """.trimIndent(),
    articleContent = """
        <p>It's been more than three weeks since the U.S. and Russia completed the largest prisoner swap since the collapse of the Soviet Union.<br/><br/>Speaking from the White House shortly after news broke that three American prisoners were headed home, President Biden described the release as an "incredible relief."<br/><br/>Russian-American journalist Alsu Kurmasheva was one of those prisoners, and she's sharing what life was like in a Russian prison and how she's adjusting to life at home. <br/><br/>For sponsor-free episodes of <em>Consider This,</em> sign up for C<em>onsider This+</em> via Apple Podcasts or at <a href="https://plus.npr.org/">plus.npr.org</a>.<br/><br/>Email us at <a href="mailto:considerthis@npr.org">considerthis@npr.org</a>.</p><img src='https://media.npr.org/include/images/tracking/npr-rss-pixel.png?story=1198913145' />
    """.trimIndent(),
    articlePubDate = "2024-08-26T20:32:01-04:00",
    articleImage = "https://media.npr.org/assets/img/2024/08/26/gettyimages-2164266488-3743ec7bca7a3dfe2a1d959d7913440c39d73824.jpg"
)
