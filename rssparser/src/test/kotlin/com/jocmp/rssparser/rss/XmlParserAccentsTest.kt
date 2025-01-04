package com.jocmp.rssparser.rss

import com.jocmp.rssparser.BaseParserTest
import com.jocmp.rssparser.model.RssImage
import java.nio.charset.Charset

class XmlParserAccentsTest  : BaseParserTest(
    charset = Charset.forName("ISO-8859-1"),
    feedPath = "feed-test-accents.xml",
    channelTitle = "UOL Noticias",
    channelLink = "http://noticias.uol.com.br/",
    channelImage = RssImage(
        url = "http://rss.i.uol.com.br/uol_rss.gif"
    ),
    channelDescription = "Últimas Notícias",
    articleTitle = "Artur Jorge não é mais técnico do Botafogo",
    articleLink = "https://noticias.uol.com.br/ultimas-noticias/afp/2025/01/03/artur-jorge-nao-e-mais-tecnico-do-botafogo.htm",
    articlePubDate = "Sex, 03 Jan 2025 23:47:02 -0300",
    articleDescription = "O português Artur Jorge não continuará como treinador do Botafogo, anunciou nesta sexta-feira (3) o atual campeão brasileiro e da Copa Libertadores.",
)
