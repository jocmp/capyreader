package com.jocmp.rssparser.atom

import com.jocmp.rssparser.BaseParserTest
import com.jocmp.rssparser.model.Media

class XmlParserAtomYouTube : BaseParserTest(
    feedPath = "atom-feed-youtube.xml",
    channelTitle = "Digital Foundry",
    channelLink = "https://www.youtube.com/channel/UC9PBzalIcEQCsiIkq36PyUA",
    articleTitle = "Digital Foundry Games of 2024: The Oliver Mackenzie Collection!",
    articlePubDate = "2025-01-01T16:07:24+00:00",
    articleLink = "https://www.youtube.com/watch?v=W_S7iKZZdHw",
    articleAuthor = "Digital Foundry",
    articleGuid = "yt:video:W_S7iKZZdHw",
    media = Media(
        title = "Digital Foundry Games of 2024: The Oliver Mackenzie Collection!",
        contentUrl = "https://www.youtube.com/v/W_S7iKZZdHw?version=3",
        thumbnailUrl = "https://i4.ytimg.com/vi/W_S7iKZZdHw/hqdefault.jpg",
        description = "As the holiday period begins to unwind, we continue to look back on an excellent 2024, with Oliver Mackenzie presenting his top five games of the prior year. Want some DF-branded tee-shirts, mugs, hoodies and various items based on DF catchphrases? Check out our store: https://store.digitalfoundry.net Join the DF Supporter Program for pristine video downloads, behind the scenes content, early access to DF Retro, early access to DF Direct Weekly and much, much more: https://bit.ly/3jEGjvx Subscribe for more Digital Foundry: http://bit.ly/DFSubscribe 00:00 Introduction 00:24 #5: Final Fantasy 7 Rebirth 02:42 #4: Parking Garage Rally Circuit 05:01 #3: Metaphor ReFantazio 07:06 #2 Persona 3 Reload 12:08 #1 ….? 16:27 Looking Ahead to 2025"
    ),
)
