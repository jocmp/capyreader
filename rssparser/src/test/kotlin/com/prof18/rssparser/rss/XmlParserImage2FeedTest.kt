/*
*   Copyright 2019 Marco Gomiero
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
*/

package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage

class XmlParserImage2FeedTest : BaseXmlParserTest(
    feedPath = "feed-test-image-2.xml",
    channelTitle = "F.C. Barcelona",
    channelLink = "https://www.mundodeportivo.com/futbol/fc-barcelona",
    channelDescription = "F.C. Barcelona",
    channelImage = RssImage(
        title = "F.C. Barcelona",
        url = "https://www.mundodeportivo.com/rsc/images/logo_MD_feed.png",
        link = "https://www.mundodeportivo.com/futbol/fc-barcelona",
        description = "Mundo Deportivo es tu diario deportivo On Line. Noticias de deporte, fútbol y del Barça",
    ),
    channelLastBuildDate = null,
    channelUpdatePeriod = null,
    channelItunesData = null,
    articleGuid = "f5c42a9c-78d9-11e9-a24c-645e8f5d185b",
    articleTitle = "Martens: “Estoy feliz y quiero seguir en el Barça”",
    articleAuthor = null,
    articleLink = "https://www.mundodeportivo.com/futbol/fc-barcelona/20190517/462298326260/martens-estoy-feliz-y-quiero-seguir-en-el-barca.html",
    articlePubDate = "Fri, 17 May 2019 21:33:59 +0200",
    articleDescription = "El diario francés l’Equipe se hizo eco ayer de una noticia de impacto: el Olympique de Lyon desea fichar a la delantera azulgrana Lieke \n" +
            "    Martens. La holandesa es una de las jugadoras más cotizadas del mundo desde que hace dos años recibió el...",
    articleContent = null,
    articleImage = "https://www.mundodeportivo.com/ra/thumbnail/GODO/MD/p6/Barca/Imagenes/2019/05/17/Recortada/img_ppunti_20190517-210423_imagenes_md_propias_ppunti_190517fcbfem294_4_6_2298222649-kYlG-U4622983262609eF-980x554@MundoDeportivo-Web.jpg",
    articleAudio = null,
    articleVideo = null,
    articleSourceName = null,
    articleSourceUrl = null,
    articleCategories = listOf("F.C. Barcelona"),
    articleCommentsUrl = null,
    articleItunesData = null,
)
