package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage

class XmlParserHindiChannelImageTest : BaseXmlParserTest(
    feedPath = "feed-test-hindi-channel-image.xml",
    channelTitle = "Latest News चीन News18 हिंदी",
    channelLink = "https://hindi.news18.com/rss/khabar/world/china.xml",
    channelDescription = "Latest news from चीन Section",
    channelImage = RssImage(
        title = "News18 हिंदी",
        url = "https://static.hindi.news18.com/ibnkhabar/uploads/2017/01/mainlogo_hindi_new.png",
        link = "https://hindi.news18.com/rss/khabar/world/china.xml",
        description = "Feed provided by News18 हिंदी",
    ),
    channelLastBuildDate = "January, 29 Jan 2021 17:56:15 +0530",
    channelUpdatePeriod = null,
    channelItunesData = null,
    articleGuid = "https://hindi.news18.com/news/world/who-experts-in-wuhan-probing-coronavirus-origins-meet-chinese-scientists-nodtg-3437941.html",
    articleTitle = "कैसे फैला कोरोना वायरस, WHO की टीम करेगी चीनी अधिकारियों के साथ बैठक",
    articleAuthor = null,
    articleLink = "https://hindi.news18.com/news/world/who-experts-in-wuhan-probing-coronavirus-origins-meet-chinese-scientists-nodtg-3437941.html",
    articlePubDate = "Friday, January 29, 2021 04:42 PM",
    articleDescription = "<img src='https://images.news18.com/ibnkhabar/uploads/2021/01/who-team-1.jpg' height='50' width='76' />कोरोना वायरस की उत्पत्ति को लेकर विश्व स्वास्थ्य संगठन (WHO) के विशेषज्ञ शुक्रवार को चीन के वुहान (Wuhan) में अधिकारियों के साथ बैठक करेंगे.",
    articleContent = null,
    articleImage = "https://images.news18.com/ibnkhabar/uploads/2021/01/who-team-1.jpg",
    articleAudio = null,
    articleVideo = null,
    articleSourceName = null,
    articleSourceUrl = null,
    articleCategories = listOf(),
    articleCommentsUrl = null,
    articleItunesData = null,
)
