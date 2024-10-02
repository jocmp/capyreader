package com.prof18.rssparser.rdf

import com.prof18.rssparser.BaseParserTest
import com.prof18.rssparser.model.RssImage

class XmlParserAtomTest: BaseParserTest(
    feedPath = "feed-rdf-test.xml",
    channelTitle = "Slashdot",
    channelLink = "https://slashdot.org/",
    channelDescription = "News for nerds, stuff that matters",
    channelImage = RssImage(
        title = null,
        link = null,
        description = null,
        url = "https://a.fsdn.com/sd/topics/topicslashdot.gif"
    ),
    channelLastBuildDate = "2024-08-22T21:44:07+00:00",
    articleTitle = "Google Play Will No Longer Pay To Discover Vulnerabilities In Popular Android Apps",
    articlePubDate = "2024-08-22T21:10:00+00:00",
    articleAuthor = "BeauHD",
    articleLink = "https://tech.slashdot.org/story/24/08/22/2042250/google-play-will-no-longer-pay-to-discover-vulnerabilities-in-popular-android-apps?utm_source=rss1.0mainlinkanon&utm_medium=feed",
    articleDescription = """
        Android Authority's Mishaal Rahman reports: Security vulnerabilities are lurking in most of the apps you use on a day-to-day basis; there's just no way for most companies to preemptively fix every possible security issue because of human error, deadlines, lack of resources, and a multitude of other factors. That's why many organizations run bug bounty programs to get external help with fixing these issues. The Google Play Security Reward Program (GPSRP) is an example of a bug bounty program that paid security researchers to find vulnerabilities in popular Android apps, but it's being shut down later this month. Google announced the Google Play Security Reward Program back in October 2017 as a way to incentivize security searchers to find and, most importantly, responsibly disclose vulnerabilities in popular Android apps distributed through the Google Play Store. [...]
         
        The purpose of the Google Play Security Reward Program was simple: Google wanted to make the Play Store a more secure destination for Android apps. According to the company, vulnerability data they collected from the program was used to help create automated checks that scanned all apps available in Google Play for similar vulnerabilities. In 2019, Google said these automated checks helped more than 300,000 developers fix more than 1,000,000 apps on Google Play. Thus, the downstream effect of the GPSRP is that fewer vulnerable apps are distributed to Android users.
         
        However, Google has now decided to wind down the Google Play Security Reward Program. In an email to participating developers, such as Sean Pesce, the company announced that the GPSRP will end on August 31st. The reason Google gave is that the program has seen a decrease in the number of actionable vulnerabilities reported. The company credits this success to the "overall increase in the Android OS security posture and feature hardening efforts."<p><div class="share_submission" style="position:relative;">
        <a class="slashpop" href="http://twitter.com/home?status=Google+Play+Will+No+Longer+Pay+To+Discover+Vulnerabilities+In+Popular+Android+Apps%3A+https%3A%2F%2Ftech.slashdot.org%2Fstory%2F24%2F08%2F22%2F2042250%2F%3Futm_source%3Dtwitter%26utm_medium%3Dtwitter"><img src="https://a.fsdn.com/sd/twitter_icon_large.png"></a>
        <a class="slashpop" href="http://www.facebook.com/sharer.php?u=https%3A%2F%2Ftech.slashdot.org%2Fstory%2F24%2F08%2F22%2F2042250%2Fgoogle-play-will-no-longer-pay-to-discover-vulnerabilities-in-popular-android-apps%3Futm_source%3Dslashdot%26utm_medium%3Dfacebook"><img src="https://a.fsdn.com/sd/facebook_icon_large.png"></a>



        </div></p><p><a href="https://tech.slashdot.org/story/24/08/22/2042250/google-play-will-no-longer-pay-to-discover-vulnerabilities-in-popular-android-apps?utm_source=rss1.0moreanon&amp;utm_medium=feed">Read more of this story</a> at Slashdot.</p><iframe src="https://slashdot.org/slashdot-it.pl?op=discuss&amp;id=23430704&amp;smallembed=1" style="height: 300px; width: 100%; border: none;"></iframe>
    """.trimIndent(),
    articleCategories = listOf("technology")
)
