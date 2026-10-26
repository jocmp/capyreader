package com.jocmp.mercury.extractors.custom

import com.jocmp.mercury.extractors.CustomExtractor
import com.jocmp.mercury.extractors.custom._247sports.com.TwoFortySevenSportsComExtractor
import com.jocmp.mercury.extractors.custom._9to5google.com.NineToFiveGoogleComExtractor
import com.jocmp.mercury.extractors.custom._9to5linux.com.NineToFiveLinuxComExtractor
import com.jocmp.mercury.extractors.custom._9to5mac.com.NineToFiveMacComExtractor
import com.jocmp.mercury.extractors.custom.abcnews.go.com.AbcnewsGoComExtractor
import com.jocmp.mercury.extractors.custom.arstechnica.com.ArstechnicaComExtractor
import com.jocmp.mercury.extractors.custom.balloon_juice.com.BalloonJuiceComExtractor
import com.jocmp.mercury.extractors.custom.biorxiv.org.BiorxivOrgExtractor
import com.jocmp.mercury.extractors.custom.blisterreview.com.BlisterreviewComExtractor
import com.jocmp.mercury.extractors.custom.blogspot.com.BloggerExtractor
import com.jocmp.mercury.extractors.custom.bookwalker.jp.BookwalkerJpExtractor
import com.jocmp.mercury.extractors.custom.bsky.app.BskyAppExtractor
import com.jocmp.mercury.extractors.custom.buzzap.jp.BuzzapJpExtractor
import com.jocmp.mercury.extractors.custom.chicagoyimby.com.ChicagoyimbyComExtractor
import com.jocmp.mercury.extractors.custom.clinicaltrials.gov.ClinicaltrialsGovExtractor
import com.jocmp.mercury.extractors.custom.deadline.com.DeadlineComExtractor
import com.jocmp.mercury.extractors.custom.deadspin.com.DeadspinExtractor
import com.jocmp.mercury.extractors.custom.economictimes.indiatimes.com.EconomictimesIndiatimesComExtractor
import com.jocmp.mercury.extractors.custom.epaper.zeit.de.EpaperZeitDeExtractor
import com.jocmp.mercury.extractors.custom.factorio.com.FactorioComExtractor
import com.jocmp.mercury.extractors.custom.fandom.wikia.com.WikiaExtractor
import com.jocmp.mercury.extractors.custom.fortune.com.FortuneComExtractor
import com.jocmp.mercury.extractors.custom.forward.com.ForwardComExtractor
import com.jocmp.mercury.extractors.custom.genius.com.GeniusComExtractor
import com.jocmp.mercury.extractors.custom.getnews.jp.GetnewsJpExtractor
import com.jocmp.mercury.extractors.custom.github.com.GithubComExtractor
import com.jocmp.mercury.extractors.custom.gonintendo.com.GonintendoComExtractor
import com.jocmp.mercury.extractors.custom.gothamist.com.GothamistComExtractor
import com.jocmp.mercury.extractors.custom.hellogiggles.com.HellogigglesComExtractor
import com.jocmp.mercury.extractors.custom.ici.radio_canada.ca.IciRadioCanadaCaExtractor
import com.jocmp.mercury.extractors.custom.japan.cnet.com.JapanCnetComExtractor
import com.jocmp.mercury.extractors.custom.japan.zdnet.com.JapanZdnetComExtractor
import com.jocmp.mercury.extractors.custom.jvndb.jvn.jp.JvndbJvnJpExtractor
import com.jocmp.mercury.extractors.custom.ma.ttias.be.MaTtiasBeExtractor
import com.jocmp.mercury.extractors.custom.mashable.com.MashableComExtractor
import com.jocmp.mercury.extractors.custom.medium.com.MediumComExtractor
import com.jocmp.mercury.extractors.custom.mobilesyrup.com.MobilesyrupComExtractor
import com.jocmp.mercury.extractors.custom.money.cnn.com.MoneyCnnComExtractor
import com.jocmp.mercury.extractors.custom.newrepublic.com.NewrepublicComExtractor
import com.jocmp.mercury.extractors.custom.news.mynavi.jp.NewsMynaviJpExtractor
import com.jocmp.mercury.extractors.custom.news.nationalgeographic.com.NewsNationalgeographicComExtractor
import com.jocmp.mercury.extractors.custom.news.pts.org.tw.NewsPtsOrgTwExtractor
import com.jocmp.mercury.extractors.custom.nymag.com.NYMagExtractor
import com.jocmp.mercury.extractors.custom.obamawhitehouse.archives.gov.ObamawhitehouseArchivesGovExtractor
import com.jocmp.mercury.extractors.custom.observer.com.ObserverComExtractor
import com.jocmp.mercury.extractors.custom.orf.at.OrfAtExtractor
import com.jocmp.mercury.extractors.custom.otrs.com.OtrsComExtractor
import com.jocmp.mercury.extractors.custom.pagesix.com.PagesixComExtractor
import com.jocmp.mercury.extractors.custom.pastebin.com.PastebinComExtractor
import com.jocmp.mercury.extractors.custom.people.com.PeopleComExtractor
import com.jocmp.mercury.extractors.custom.phpspot.org.PhpspotOrgExtractor
import com.jocmp.mercury.extractors.custom.pitchfork.com.PitchforkComExtractor
import com.jocmp.mercury.extractors.custom.polskisamorzad.se.pl.PolskisamorzadSePlExtractor
import com.jocmp.mercury.extractors.custom.qz.com.QzComExtractor
import com.jocmp.mercury.extractors.custom.scan.netsecurity.ne.jp.ScanNetsecurityNeJpExtractor
import com.jocmp.mercury.extractors.custom.sciencefly.com.ScienceflyComExtractor
import com.jocmp.mercury.extractors.custom.sect.iij.ad.jp.SectIijAdJpExtractor
import com.jocmp.mercury.extractors.custom.sg.news.yahoo.com.SgNewsYahooComExtractor
import com.jocmp.mercury.extractors.custom.superseriale.se.pl.SuperserialeSePlExtractor
import com.jocmp.mercury.extractors.custom.takagi_hiromitsu.jp.TakagiHiromitsuJpExtractor
import com.jocmp.mercury.extractors.custom.tarnkappe.info.TarnkappeInfoExtractor
import com.jocmp.mercury.extractors.custom.techcrunch.com.TechcrunchComExtractor
import com.jocmp.mercury.extractors.custom.techlog.iij.ad.jp.TechlogIijAdJpExtractor
import com.jocmp.mercury.extractors.custom.terminaltrove.com.TerminaltroveComExtractor
import com.jocmp.mercury.extractors.custom.thefederalistpapers.org.ThefederalistpapersOrgExtractor
import com.jocmp.mercury.extractors.custom.thoughtcatalog.com.ThoughtcatalogComExtractor
import com.jocmp.mercury.extractors.custom.timesofindia.indiatimes.com.TimesofindiaIndiatimesComExtractor
import com.jocmp.mercury.extractors.custom.tldr.tech.TldrTechExtractor
import com.jocmp.mercury.extractors.custom.twitter.com.TwitterComExtractor
import com.jocmp.mercury.extractors.custom.uproxx.com.UproxxComExtractor
import com.jocmp.mercury.extractors.custom.wccftech.com.WccftechComExtractor
import com.jocmp.mercury.extractors.custom.weekly.ascii.jp.WeeklyAsciiJpExtractor
import com.jocmp.mercury.extractors.custom.wikipedia.org.WikipediaOrgExtractor
import com.jocmp.mercury.extractors.custom.wired.jp.WiredJpExtractor
import com.jocmp.mercury.extractors.custom.www._1pezeshk.com.Www1pezeshkComExtractor
import com.jocmp.mercury.extractors.custom.www.abendblatt.de.WwwAbendblattDeExtractor
import com.jocmp.mercury.extractors.custom.www.al.com.WwwAlComExtractor
import com.jocmp.mercury.extractors.custom.www.americanow.com.WwwAmericanowComExtractor
import com.jocmp.mercury.extractors.custom.www.androidauthority.com.WwwAndroidauthorityComExtractor
import com.jocmp.mercury.extractors.custom.www.androidcentral.com.WwwAndroidcentralComExtractor
import com.jocmp.mercury.extractors.custom.www.aol.com.WwwAolComExtractor
import com.jocmp.mercury.extractors.custom.www.apartmenttherapy.com.ApartmentTherapyExtractor
import com.jocmp.mercury.extractors.custom.www.asahi.com.WwwAsahiComExtractor
import com.jocmp.mercury.extractors.custom.www.blick.de.WwwBlickDeExtractor
import com.jocmp.mercury.extractors.custom.www.bloomberg.com.BloombergExtractor
import com.jocmp.mercury.extractors.custom.www.broadwayworld.com.BroadwayWorldExtractor
import com.jocmp.mercury.extractors.custom.www.bustle.com.WwwBustleComExtractor
import com.jocmp.mercury.extractors.custom.www.buzzfeed.com.BuzzfeedExtractor
import com.jocmp.mercury.extractors.custom.www.cbc.ca.WwwCbcCaExtractor
import com.jocmp.mercury.extractors.custom.www.cbssports.com.WwwCbssportsComExtractor
import com.jocmp.mercury.extractors.custom.www.channelnewsasia.com.WwwChannelnewsasiaComExtractor
import com.jocmp.mercury.extractors.custom.www.chicagotribune.com.WwwChicagotribuneComExtractor
import com.jocmp.mercury.extractors.custom.www.cnbc.com.WwwCnbcComExtractor
import com.jocmp.mercury.extractors.custom.www.cnet.com.WwwCnetComExtractor
import com.jocmp.mercury.extractors.custom.www.cnn.com.WwwCnnComExtractor
import com.jocmp.mercury.extractors.custom.www.digitalfoundry.net.WwwDigitalfoundryNetExtractor
import com.jocmp.mercury.extractors.custom.www.dmagazine.com.WwwDmagazineComExtractor
import com.jocmp.mercury.extractors.custom.www.elecom.co.jp.WwwElecomCoJpExtractor
import com.jocmp.mercury.extractors.custom.www.engadget.com.WwwEngadgetComExtractor
import com.jocmp.mercury.extractors.custom.www.eonline.com.WwwEonlineComExtractor
import com.jocmp.mercury.extractors.custom.www.euronews.com.WwwEuronewsComExtractor
import com.jocmp.mercury.extractors.custom.www.fastcompany.com.WwwFastcompanyComExtractor
import com.jocmp.mercury.extractors.custom.www.flatpanelshd.com.WwwFlatpanelshdComExtractor
import com.jocmp.mercury.extractors.custom.www.fool.com.WwwFoolComExtractor
import com.jocmp.mercury.extractors.custom.www.fortinet.com.WwwFortinetComExtractor
import com.jocmp.mercury.extractors.custom.www.futura_sciences.com.WwwFuturaSciencesComExtractor
import com.jocmp.mercury.extractors.custom.www.gizmodo.jp.WwwGizmodoJpExtractor
import com.jocmp.mercury.extractors.custom.www.gruene.de.WwwGrueneDeExtractor
import com.jocmp.mercury.extractors.custom.www.hardwarezone.com.sg.WwwHardwarezoneComSgExtractor
import com.jocmp.mercury.extractors.custom.www.heise.de.WwwHeiseDeExtractor
import com.jocmp.mercury.extractors.custom.www.huffingtonpost.com.WwwHuffingtonpostComExtractor
import com.jocmp.mercury.extractors.custom.www.ilfattoquotidiano.it.WwwIlfattoquotidianoItExtractor
import com.jocmp.mercury.extractors.custom.www.infoq.com.WwwInfoqComExtractor
import com.jocmp.mercury.extractors.custom.www.inquisitr.com.WwwInquisitrComExtractor
import com.jocmp.mercury.extractors.custom.www.investmentexecutive.com.WwwInvestmentexecutiveComExtractor
import com.jocmp.mercury.extractors.custom.www.ipa.go.jp.WwwIpaGoJpExtractor
import com.jocmp.mercury.extractors.custom.www.itmedia.co.jp.WwwItmediaCoJpExtractor
import com.jocmp.mercury.extractors.custom.www.jalopnik.com.WwwJalopnikComExtractor
import com.jocmp.mercury.extractors.custom.www.jnsa.org.WwwJnsaOrgExtractor
import com.jocmp.mercury.extractors.custom.www.ladbible.com.WwwLadbibleComExtractor
import com.jocmp.mercury.extractors.custom.www.latimes.com.WwwLatimesComExtractor
import com.jocmp.mercury.extractors.custom.www.lebensmittelwarnung.de.WwwLebensmittelwarnungDeExtractor
import com.jocmp.mercury.extractors.custom.www.lemonde.fr.WwwLemondeFrExtractor
import com.jocmp.mercury.extractors.custom.www.lifehacker.jp.WwwLifehackerJpExtractor
import com.jocmp.mercury.extractors.custom.www.linkedin.com.WwwLinkedinComExtractor
import com.jocmp.mercury.extractors.custom.www.littlethings.com.LittleThingsExtractor
import com.jocmp.mercury.extractors.custom.www.macrumors.com.WwwMacrumorsComExtractor
import com.jocmp.mercury.extractors.custom.www.mentalfloss.com.WwwMentalflossComExtractor
import com.jocmp.mercury.extractors.custom.www.miamiherald.com.WwwMiamiheraldComExtractor
import com.jocmp.mercury.extractors.custom.www.moongift.jp.WwwMoongiftJpExtractor
import com.jocmp.mercury.extractors.custom.www.msn.com.MSNExtractor
import com.jocmp.mercury.extractors.custom.www.msnbc.com.WwwMsnbcComExtractor
import com.jocmp.mercury.extractors.custom.www.n_tv.de.WwwNtvDeExtractor
import com.jocmp.mercury.extractors.custom.www.nationalgeographic.com.WwwNationalgeographicComExtractor
import com.jocmp.mercury.extractors.custom.www.nbcnews.com.WwwNbcnewsComExtractor
import com.jocmp.mercury.extractors.custom.www.ndtv.com.WwwNdtvComExtractor
import com.jocmp.mercury.extractors.custom.www.newyorker.com.NewYorkerExtractor
import com.jocmp.mercury.extractors.custom.www.notebookcheck.net.WwwNotebookcheckNetExtractor
import com.jocmp.mercury.extractors.custom.www.npr.org.WwwNprOrgExtractor
import com.jocmp.mercury.extractors.custom.www.numerama.com.WwwNumeramaComExtractor
import com.jocmp.mercury.extractors.custom.www.nydailynews.com.WwwNydailynewsComExtractor
import com.jocmp.mercury.extractors.custom.www.nytimes.com.NYTimesExtractor
import com.jocmp.mercury.extractors.custom.www.opposingviews.com.WwwOpposingviewsComExtractor
import com.jocmp.mercury.extractors.custom.www.oreilly.co.jp.WwwOreillyCoJpExtractor
import com.jocmp.mercury.extractors.custom.www.ossnews.jp.WwwOssnewsJpExtractor
import com.jocmp.mercury.extractors.custom.www.phoronix.com.WwwPhoronixComExtractor
import com.jocmp.mercury.extractors.custom.www.politico.com.PoliticoExtractor
import com.jocmp.mercury.extractors.custom.www.polygon.com.WwwPolygonComExtractor
import com.jocmp.mercury.extractors.custom.www.popsugar.com.WwwPopsugarComExtractor
import com.jocmp.mercury.extractors.custom.www.prospectmagazine.co.uk.WwwProspectmagazineCoUkExtractor
import com.jocmp.mercury.extractors.custom.www.publickey1.jp.WwwPublickey1JpExtractor
import com.jocmp.mercury.extractors.custom.www.qbitai.com.WwwQbitaiComExtractor
import com.jocmp.mercury.extractors.custom.www.qdaily.com.WwwQdailyComExtractor
import com.jocmp.mercury.extractors.custom.www.rawstory.com.WwwRawstoryComExtractor
import com.jocmp.mercury.extractors.custom.www.rbbtoday.com.WwwRbbtodayComExtractor
import com.jocmp.mercury.extractors.custom.www.recode.net.WwwRecodeNetExtractor
import com.jocmp.mercury.extractors.custom.www.reddit.com.WwwRedditComExtractor
import com.jocmp.mercury.extractors.custom.www.refinery29.com.WwwRefinery29ComExtractor
import com.jocmp.mercury.extractors.custom.www.reuters.com.WwwReutersComExtractor
import com.jocmp.mercury.extractors.custom.www.rollingstone.com.WwwRollingstoneComExtractor
import com.jocmp.mercury.extractors.custom.www.sanwa.co.jp.WwwSanwaCoJpExtractor
import com.jocmp.mercury.extractors.custom.www.sbnation.com.WwwSbnationComExtractor
import com.jocmp.mercury.extractors.custom.www.se.pl.WwwSePlExtractor
import com.jocmp.mercury.extractors.custom.www.si.com.WwwSiComExtractor
import com.jocmp.mercury.extractors.custom.www.slate.com.WwwSlateComExtractor
import com.jocmp.mercury.extractors.custom.www.spektrum.de.SpektrumExtractor
import com.jocmp.mercury.extractors.custom.www.spiegel.de.WwwSpiegelDeExtractor
import com.jocmp.mercury.extractors.custom.www.tagesschau.de.WwwTagesschauDeExtractor
import com.jocmp.mercury.extractors.custom.www.techpowerup.com.WwwTechpowerupComExtractor
import com.jocmp.mercury.extractors.custom.www.theatlantic.com.TheAtlanticExtractor
import com.jocmp.mercury.extractors.custom.www.thedrive.com.WwwThedriveComExtractor
import com.jocmp.mercury.extractors.custom.www.theguardian.com.TheGuardianExtractor
import com.jocmp.mercury.extractors.custom.www.thepennyhoarder.com.WwwThepennyhoarderComExtractor
import com.jocmp.mercury.extractors.custom.www.thepoliticalinsider.com.WwwThepoliticalinsiderComExtractor
import com.jocmp.mercury.extractors.custom.www.theverge.com.WwwThevergeComExtractor
import com.jocmp.mercury.extractors.custom.www.tmz.com.WwwTmzComExtractor
import com.jocmp.mercury.extractors.custom.www.today.com.WwwTodayComExtractor
import com.jocmp.mercury.extractors.custom.www.transfermarkt.de.WwwTransfermarktDeExtractor
import com.jocmp.mercury.extractors.custom.www.usmagazine.com.WwwUsmagazineComExtractor
import com.jocmp.mercury.extractors.custom.www.versants.com.WwwVersantsComExtractor
import com.jocmp.mercury.extractors.custom.www.videogameschronicle.com.WwwVideogameschronicleComExtractor
import com.jocmp.mercury.extractors.custom.www.vortez.net.WwwVortezNetExtractor
import com.jocmp.mercury.extractors.custom.www.vox.com.WwwVoxComExtractor
import com.jocmp.mercury.extractors.custom.www.washingtonpost.com.WashingtonPostExtractor
import com.jocmp.mercury.extractors.custom.www.westernjournalism.com.WwwWesternjournalismComExtractor
import com.jocmp.mercury.extractors.custom.www.wired.com.WiredExtractor
import com.jocmp.mercury.extractors.custom.www.yahoo.com.YahooExtractor
import com.jocmp.mercury.extractors.custom.www.yomiuri.co.jp.WwwYomiuriCoJpExtractor
import com.jocmp.mercury.extractors.custom.www.youtube.com.WwwYoutubeComExtractor

/**
 * Central listing of every custom extractor. Each ported site adds itself
 * to [all] and the registry pulls them in on first access. Keeps registration
 * deterministic (no SPI / classpath scanning) and trivial to grep.
 */
object AllExtractors {
    val all: List<CustomExtractor> =
        listOf(
            AbcnewsGoComExtractor,
            ArstechnicaComExtractor,
            BalloonJuiceComExtractor,
            BiorxivOrgExtractor,
            BlisterreviewComExtractor,
            BloggerExtractor,
            BloombergExtractor,
            BookwalkerJpExtractor,
            BskyAppExtractor,
            BuzzapJpExtractor,
            ChicagoyimbyComExtractor,
            ClinicaltrialsGovExtractor,
            DeadlineComExtractor,
            DeadspinExtractor,
            EconomictimesIndiatimesComExtractor,
            EpaperZeitDeExtractor,
            FactorioComExtractor,
            FortuneComExtractor,
            ForwardComExtractor,
            GeniusComExtractor,
            GetnewsJpExtractor,
            GithubComExtractor,
            GonintendoComExtractor,
            GothamistComExtractor,
            HellogigglesComExtractor,
            IciRadioCanadaCaExtractor,
            JapanCnetComExtractor,
            JapanZdnetComExtractor,
            JvndbJvnJpExtractor,
            MaTtiasBeExtractor,
            MashableComExtractor,
            MediumComExtractor,
            MobilesyrupComExtractor,
            MoneyCnnComExtractor,
            NewrepublicComExtractor,
            NewsMynaviJpExtractor,
            NewsNationalgeographicComExtractor,
            NewsPtsOrgTwExtractor,
            NineToFiveGoogleComExtractor,
            NineToFiveLinuxComExtractor,
            NineToFiveMacComExtractor,
            NYMagExtractor,
            NYTimesExtractor,
            ObamawhitehouseArchivesGovExtractor,
            ObserverComExtractor,
            OrfAtExtractor,
            OtrsComExtractor,
            PagesixComExtractor,
            PastebinComExtractor,
            PeopleComExtractor,
            PhpspotOrgExtractor,
            PitchforkComExtractor,
            PolskisamorzadSePlExtractor,
            QzComExtractor,
            ScanNetsecurityNeJpExtractor,
            ScienceflyComExtractor,
            SectIijAdJpExtractor,
            SgNewsYahooComExtractor,
            SuperserialeSePlExtractor,
            TakagiHiromitsuJpExtractor,
            TarnkappeInfoExtractor,
            TechcrunchComExtractor,
            TechlogIijAdJpExtractor,
            TerminaltroveComExtractor,
            TheAtlanticExtractor,
            TheGuardianExtractor,
            ThefederalistpapersOrgExtractor,
            ThoughtcatalogComExtractor,
            TimesofindiaIndiatimesComExtractor,
            TldrTechExtractor,
            TwitterComExtractor,
            TwoFortySevenSportsComExtractor,
            UproxxComExtractor,
            WashingtonPostExtractor,
            WccftechComExtractor,
            WeeklyAsciiJpExtractor,
            WikiaExtractor,
            WikipediaOrgExtractor,
            Www1pezeshkComExtractor,
            ApartmentTherapyExtractor,
            BroadwayWorldExtractor,
            BuzzfeedExtractor,
            WwwAbendblattDeExtractor,
            WwwAlComExtractor,
            WwwAmericanowComExtractor,
            WwwAndroidauthorityComExtractor,
            WwwAndroidcentralComExtractor,
            WwwAolComExtractor,
            WwwAsahiComExtractor,
            WwwBlickDeExtractor,
            WwwBustleComExtractor,
            WwwCbcCaExtractor,
            WwwCbssportsComExtractor,
            WwwChannelnewsasiaComExtractor,
            WwwChicagotribuneComExtractor,
            WwwCnbcComExtractor,
            WwwCnetComExtractor,
            WwwDigitalfoundryNetExtractor,
            WwwDmagazineComExtractor,
            WwwElecomCoJpExtractor,
            WwwEngadgetComExtractor,
            WwwEonlineComExtractor,
            WwwEuronewsComExtractor,
            WwwFastcompanyComExtractor,
            WwwFlatpanelshdComExtractor,
            WwwFoolComExtractor,
            WwwFortinetComExtractor,
            WwwFuturaSciencesComExtractor,
            WwwGizmodoJpExtractor,
            WwwGrueneDeExtractor,
            WwwHardwarezoneComSgExtractor,
            WwwHeiseDeExtractor,
            WwwHuffingtonpostComExtractor,
            WwwIlfattoquotidianoItExtractor,
            WwwInfoqComExtractor,
            WwwInquisitrComExtractor,
            WwwInvestmentexecutiveComExtractor,
            WwwIpaGoJpExtractor,
            WwwItmediaCoJpExtractor,
            WwwJnsaOrgExtractor,
            WwwLadbibleComExtractor,
            WwwLatimesComExtractor,
            WwwLebensmittelwarnungDeExtractor,
            WwwLemondeFrExtractor,
            WwwLifehackerJpExtractor,
            WwwLinkedinComExtractor,
            LittleThingsExtractor,
            WwwMacrumorsComExtractor,
            WwwMentalflossComExtractor,
            WwwMiamiheraldComExtractor,
            WwwMoongiftJpExtractor,
            MSNExtractor,
            WwwMsnbcComExtractor,
            WwwNtvDeExtractor,
            WwwNationalgeographicComExtractor,
            WwwNbcnewsComExtractor,
            WwwNdtvComExtractor,
            NewYorkerExtractor,
            WwwNotebookcheckNetExtractor,
            WwwNprOrgExtractor,
            WwwNumeramaComExtractor,
            WwwNydailynewsComExtractor,
            WwwOpposingviewsComExtractor,
            WwwOreillyCoJpExtractor,
            WwwOssnewsJpExtractor,
            WwwPhoronixComExtractor,
            PoliticoExtractor,
            WwwPolygonComExtractor,
            WwwPopsugarComExtractor,
            WwwProspectmagazineCoUkExtractor,
            WwwPublickey1JpExtractor,
            WwwQbitaiComExtractor,
            WwwQdailyComExtractor,
            WwwRawstoryComExtractor,
            WwwRbbtodayComExtractor,
            WwwRecodeNetExtractor,
            WwwRedditComExtractor,
            WwwRefinery29ComExtractor,
            WwwReutersComExtractor,
            WwwRollingstoneComExtractor,
            WwwSanwaCoJpExtractor,
            WwwSbnationComExtractor,
            WwwSiComExtractor,
            WwwSlateComExtractor,
            SpektrumExtractor,
            WwwSpiegelDeExtractor,
            WwwTagesschauDeExtractor,
            WwwTechpowerupComExtractor,
            WwwThedriveComExtractor,
            WwwThepennyhoarderComExtractor,
            WwwThepoliticalinsiderComExtractor,
            WwwTmzComExtractor,
            WwwTodayComExtractor,
            WwwTransfermarktDeExtractor,
            WwwUsmagazineComExtractor,
            WwwVersantsComExtractor,
            WwwVideogameschronicleComExtractor,
            WwwVortezNetExtractor,
            WwwVoxComExtractor,
            WwwWesternjournalismComExtractor,
            YahooExtractor,
            WwwYomiuriCoJpExtractor,
            WwwYoutubeComExtractor,
            WwwSePlExtractor,
            WiredExtractor,
            WiredJpExtractor,
            WwwCnnComExtractor,
            WwwJalopnikComExtractor,
            WwwThevergeComExtractor,
        )
}
