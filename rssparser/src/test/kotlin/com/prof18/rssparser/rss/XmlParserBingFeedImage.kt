package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage

class XmlParserBingFeedImage : BaseXmlParserTest(
    feedPath = "feed-bing-image.xml",
    channelTitle = "madrid - BingNews",
    channelLink = "https://www.bing.com:443/news/search?q=madrid&format=rss",
    channelDescription = "Search results",
    channelImage = RssImage(
        title = "madrid",
        url = "http://www.bing.com/rsslogo.gif",
        link = "https://www.bing.com:443/news/search?q=madrid&format=rss",
        description = null
    ),
    articleTitle = "Real Madrid, Barcelona and Juventus 'threaten clubs withdrawing from Super League\n" +
            "                with legal action'",
    articleLink = "http://www.bing.com/news/apiclick.aspx?ref=FexRss&aid=&tid=12BE3CE268B0484F92DD9828C685E325&url=https%3a%2f%2fwww.dailymail.co.uk%2fsport%2ffootball%2farticle-9554407%2fReal-Madrid-Barcelona-Juventus-threaten-clubs-withdrawing-Super-League-legal-action.html&c=4438839993362862681&mkt=it-it",
    articlePubDate = "Fri, 07 May 2021 12:00:00 GMT",
    articleDescription = "The three European giants are refusing to let the plans die and have\n" +
            "                warned their former partners they will extract millions of dollars in penalties if\n" +
            "                they walk away from the league.",
    articleImage = "http://www.bing.com/th?id=OVFT.SNpH_QAbpZOYgrEHZRyCTi&pid=News",
)
