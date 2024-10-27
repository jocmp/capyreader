module Articles
  Article = Struct.new(
    :feed_name,
    :feed_link,
    :feed_link_title,
    :external_link,
    :title,
    :byline,
    :text_size,
    :body,
    :script,
    keyword_init: true
  )

  def self.find(slug)
    public_send(slug)
  end

  def self.nine_to_five_google
    Article.new(
      title: "Pixel 9, Fold 2, Tablet 2 with 5G reportedly get new Samsung modem with satellite connectivity",
      feed_name: "9to5google",
      byline: "April 14, 2024 at 8:35 AM by Ben Schoon",
      external_link: "https://9to5google.com/2024/04/14/google-pixel-9-fold-2-tablet-new-samsung-modem/",
      body: "<div class=\"container med post-content\"> <figure class=\"img-border featured-image\"> <img width=\"1600\" src=\"https://9to5google.com/wp-content/uploads/sites/4/2024/03/pixel-satellite-sos-1.jpg?quality=82&amp;strip=all&amp;w=1600\" class=\"skip-lazy wp-post-image\" alt srcset=\"https://i0.wp.com/9to5google.com/wp-content/uploads/sites/4/2024/03/pixel-satellite-sos-1.jpg?w=320&amp;quality=82&amp;strip=all&amp;ssl=1 320w, https://i0.wp.com/9to5google.com/wp-content/uploads/sites/4/2024/03/pixel-satellite-sos-1.jpg?w=640&amp;quality=82&amp;strip=all&amp;ssl=1 640w, https://i0.wp.com/9to5google.com/wp-content/uploads/sites/4/2024/03/pixel-satellite-sos-1.jpg?w=1024&amp;quality=82&amp;strip=all&amp;ssl=1 1024w, https://i0.wp.com/9to5google.com/wp-content/uploads/sites/4/2024/03/pixel-satellite-sos-1.jpg?w=1500&amp;quality=82&amp;strip=all&amp;ssl=1 1500w\"></figure> <p>According to a new report, Google&#x2019;s upcoming Pixel hardware including the Pixel 9 series, Pixel Fold 2, and a 5G-connected Pixel Tablet will be getting a new cellular modem, an overdue upgrade.</p><span class=\"article__body--subheading\">Testing subheading</span> <span id=\"more-614622\"></span> <p>Since <a href=\"https://9to5google.com/2024/02/17/google-pixel-tensor-reboot-final-stage/\">the big Tensor reboot</a>, Google has shifted away from Qualcomm&#x2019;s proven modems paired with Snapdragon processors over to Samsung modems which work with <a href=\"https://9to5google.com/2021/11/03/google-tensor-exynos-tests-deep-dive/\">the Exynos base</a> that Tensor relies on. Google first upgraded the modem paired with Tensor <a href=\"https://9to5google.com/2022/02/18/pixel-7-pro-details-2nd-gen-tensor-samsung-modem/\">on the Pixel 7 series</a> but leaned on the same modem (albeit a slightly newer refresh of it) <a href=\"https://9to5google.com/2022/11/08/tensor-g3-samsung-modem-report/\">on the Pixel 8 series</a>. And while the modem <a href=\"https://9to5google.com/2022/12/09/google-pixel-7-tensor-g2-signal-modem/\">is reasonably sufficient</a>, it still struggles in areas with low connection, and is less power efficient. </p> <p>Now, it seems Google is planning to equip Pixel 9, and other devices, with an updated modem.</p> <p><a href=\"https://www.androidauthority.com/pixel-9-sos-satellite-connectivity-3433498/\"><em>Android Authority</em> reports</a> that Pixel 9, Pixel 9 Pro, and Pixel 9 Pro &#x201C;XL&#x201D; will be equipped with a new modem. However, that modem, which will be a part of Tensor G4, will still be provided by Samsung &#x2013; the new generation is signified 5400, up from the 5300 paired with Tensor G3.</p> <p>The same modem will apparently be used in the upcoming <a href=\"https://9to5google.com/guides/google-pixel-fold-2/\">Pixel Fold 2</a> as well as a previously unknown 5G-connected Pixel Tablet. In February, <a href=\"https://9to5google.com/2024/02/07/possible-pixel-tablet-2-codenames-surface/\"><em>9to5Google</em> reported on possible Pixel Tablet 2 codenames</a>, including the &#x201C;clementine&#x201D; codename mentioned in this report, confirming that this is in fact a sequel to the Pixel Tablet. The current Pixel Tablet offers no cellular connectivity.</p> <p>The new modem is reportedly upgraded on the software side, but it&#x2019;s unclear how the hardware is changing. The only clear upgrade is support for the 3GPP Rel. 17 5G spec which supports satellite connectivity. <em>Technically</em>, the existing modem in Pixel devices <a href=\"https://9to5google.com/2023/02/23/samsung-satellite-connectivity-exynos/\">does also support satellite</a>, but it&#x2019;s not yet been used, and it does use an older version of the 5G spec.</p> <p>Alongside this added support, Pixels with satellite support would use a &#x201C;Satellite Gateway&#x201D; app which, to the user, would be used through Emergency SOS. According to this report, users will be asked basic questions to identify the emergency situation, given a choice to notify contacts, and send messages with emergency services. </p> <p>Some of the questions, which may have multiple-choice answers, include:</p> <ul>\n<li>What happened?</li> <li>[Are you/Are they/Is everyone] breathing?</li> <li>In total, how many people are [missing/trapped]?</li> <li>What best describes your situation?</li> <li>What is on fire?</li> <li>Are there weapons involved?</li> <li>What type of vehicle or vessel?</li> <li>Do any of these apply?</li>\n</ul> <p>A new preview clip, as seen below, signifies there will be a UI to help align with a satellite.</p> <figure class=\"wp-block-video\"><video src=\"https://www.androidauthority.com/wp-content/uploads/2024/04/hgfds-6000.mp4\"></video></figure> <p>Code also suggests that Pixel Fold 2 may need to be unfolded to do this.</p> <p>Google <a href=\"https://9to5google.com/2024/03/02/google-pixel-satellite-sos-feature/\">has been working on &#x201C;Satellite SOS&#x201D; on Pixel devices as of late</a>, but no functionality is live today.</p> <p>How this new modem sticks the landing will be interesting, but there&#x2019;s a pretty low bar for success here. Google is expected to shift to a TSMC-produced Tensor chip in 2025, but it&#x2019;s unclear if the company will change modems at that point. Tensor G4 is, beyond this modem, expected to be a <a href=\"https://9to5google.com/2023/09/17/google-tensor-g4-minor-update-report/\">relatively minor change</a>.</p> <p><em><strong>Follow Ben:</strong>&#xA0;<a href=\"https://twitter.com/NexusBen\">Twitter/X</a>,&#xA0;<a href=\"https://www.threads.net/@nexusben\">Threads</a>, and&#xA0;<a href=\"https://www.instagram.com/nexusben\">Instagram</a></em></p> <div class=\"ad-disclaimer-container\"><p class=\"disclaimer-affiliate\"><em>FTC: We use income earning auto affiliate links.</em> <a href=\"https://9to5mac.com/about/#affiliate\">More.</a></p></div> </div>"
    )
  end

  # Displays how figures work
  def self.the_verge
    Article.new(
      title: "The first Apple-approved emulators for the iPhone have arrived",
      feed_name: "The Verge - All Posts",
      byline: "April 14, 2024 at 9:44 AM by Wes Davis",
      external_link: "https://9to5google.com/2024/04/14/google-pixel-9-fold-2-tablet-new-samsung-modem/",
      body: "<figure>\n      <img alt=\"A screenshot of iGBA running Mario Vs. Donkey Kong for the GBA.\" src=\"https://cdn.vox-cdn.com/thumbor/PNk9V9CtejGIlexkSb3117puNtg=/227x0:4774x3031/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/73277925/iGBA_game.0.png\">\n        <figcaption><em>A screenshot from iGBA.</em> | Screenshot: Wes Davis / The Verge</figcaption>\n    </figure>\n\n  <p id=\"3AvR1j\">I played Game Boy Advance games on my iPhone last night thanks to a new emulator called iGBA, which appears to be the first Game Boy Advance emulator on the App Store since Apple started <a href=\"https://www.theverge.com/2024/4/5/24122341/apple-app-store-game-emulators-super-apps\">allowing emulators</a> worldwide. The only trouble is, it doesn’t look like iGBA is developer Mattia La Spina’s own work.</p>\n<p id=\"LT7Xiw\">In an email to <em>The Verge</em>, developer Riley Testut said the app is an unauthorized clone of GBA4iOS, the open-source emulator he created <a href=\"https://www.theverge.com/2014/7/31/5956059/you-can-play-every-game-boy-advance-game-on-your-iphone-right-now\">for iOS over a decade ago</a> (and <a href=\"https://www.theverge.com/2024/2/19/24077846/if-youve-got-a-vision-pro-you-can-now-play-a-giant-game-boy\">recently resurrected for the Vision Pro</a>). He said his app uses the <a href=\"https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html\">GNU GPLv2 license</a>. A <a href=\"https://mastodon.social/@maczydeco/112268422489536936\">Mastodon user found</a> that iGBA does not reference the license, which may violate its terms.</p>\n<p id=\"LOiHjm\">Despite that, <blockquote>he says it’s Apple he’s frustrated with, not La Spina.</blockquote></p>\n<div id=\"IIiBf4\">\n<div><div><a href=\"https://www.threads.net/@rileytestut/post/C5u935tNaPs\" data-iframely-url=\"https://cdn.iframe.ly/api/iframe?url=https%3A%2F%2Fwww.threads.net%2F%40rileytestut%2Fpost%2FC5u935tNaPs&amp;key=9ef4a209439e42bc59783ba959d50197\"></a></div></div>\n\n</div>\n<p id=\"M7zjkX\">Testut also provided...</p>\n  <p>\n    <a href=\"https://www.theverge.com/2024/4/14/24129981/game-boy-emulator-igba-iphone-ios-app-store-gba4ios-testut-knock-off\">Continue reading…</a>\n  </p>"
    )
  end

  # Article with table
  def self.arstechnica
    Article.new(
      title: "Review: Framework’s Laptop 16 is unique, laudable, fascinating, and flawed",
      feed_name: "Ars Technica",
      external_link: "https://arstechnica.com/?p=1994846",
      byline: "January 29th, 2024 at 5:35 AM by Andrew Cunningham",
      body: "<div><div class=\"article-content article-body clearfix\">\n      \n<figure class=\"intro-image image full\">\n      <img src=\"https://cdn.arstechnica.net/wp-content/uploads/2024/01/IMG_1635-980x653.jpeg\" width=\"980\">\n  \n<figcaption class=\"caption\"><div class=\"caption-text\"><a href=\"https://cdn.arstechnica.net/wp-content/uploads/2024/01/IMG_1635.jpeg\" class=\"enlarge\">Enlarge</a>  The Framework Laptop 16.</div></figcaption>\n</figure>\n\n\n\n\n\n<table class=\"specifications right\" width=\"300\">\n<tbody>\n<tr>\n<th>Specs at a glance: Framework Laptop 16</th>\n</tr>\n<tr>\n<th>OS</th>\n<td>Windows 11 23H2</td>\n</tr>\n<tr>\n<th>CPU</th>\n<td>AMD Ryzen 7 7940HS (8-cores)</td>\n</tr>\n<tr>\n<th>RAM</th>\n<td>32GB DDR5-5600 (upgradeable)</td>\n</tr>\n<tr>\n<th>GPU</th>\n<td>AMD Radeon 780M (integrated)/AMD Radeon RX 7700S (dedicated)</td>\n</tr>\n<tr>\n<th>SSD</th>\n<td>1TB Western Digital Black SN770</td>\n</tr>\n<tr>\n<th>Battery</th>\n<td>85 WHr</td>\n</tr>\n<tr>\n<th>Display</th>\n<td>16-inch 2560x1600 165 Hz matte non-touchscreen</td>\n</tr>\n<tr>\n<th>Connectivity</th>\n<td>6x recessed USB-C ports (2x USB 4, 4x USB 3.2) with customizable &quot;Expansion Card&quot; dongles</td>\n</tr>\n<tr>\n<th>Weight</th>\n<td>4.63 pounds (2.1 kg) without GPU, 5.29 pounds (2.4 kg) with GPU</td>\n</tr>\n<tr>\n<th>Price as tested</th>\n<td><a href=\"https://frame.work/products/laptop16-amd-7040/configuration/new\">$2,499</a> pre-built, <a href=\"https://frame.work/products/laptop16-diy-amd-7040/configuration/new\">$2,421</a> DIY edition with no OS</td>\n</tr>\n</tbody>\n</table>\n<p>Now that the Framework Laptop 13 has been through three refresh cycles&#x2014;including one that swapped from Intel&apos;s CPUs to AMD&apos;s within the exact same body&#x2014;the company is setting its sights on something bigger.</p>\n<p>Today, we&apos;re taking an extended look at the first Framework Laptop 16, which wants to do for a workstation/gaming laptop what the Framework Laptop 13 did for thin-and-light ultraportables. In some ways, the people who use these kinds of systems need a Framework Laptop most of all; they&apos;re an even bigger investment than a thin-and-light laptop, and a single CPU, GPU, memory, or storage upgrade can extend the useful life of the system for years, just like upgrading a desktop.</p>\n<p>The Laptop 16 melds ideas from the original Framework Laptop with some all-new mechanisms for customizing the device&apos;s keyboard, adding and upgrading a dedicated GPU, and installing other modules. The result is a relatively bulky and heavy laptop compared to many of its non-upgradeable alternatives. And you&apos;ll need to trust that Framework delivers on its upgradeability promises somewhere down the line since the current options for upgrading and expanding the laptop are fairly limited.</p>\n<p>But the company has done a great job of building trust with the Framework Laptop 13&#x2014;if you don&apos;t mind the design of the Laptop 16, there&apos;s a reasonably good chance you&apos;ll have appealing upgrades to grab. In a year or two.</p>\n<h3>Table of Contents</h3>\n\n\n\n    </div></div>"
    )
  end

  # Extract content locally
  def self.extracted_content
    script = <<~SCRIPT
      <script>
        (() => {
          let html = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur vel nibh nisl. Proin et varius enim. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam volutpat ut sapien sit amet cursus. Donec porta lacinia lectus. Suspendisse vel malesuada metus. Morbi velit est, ullamcorper in luctus vel, ornare sit amet turpis. Cras at libero at nisl sollicitudin semper eu ut odio. Nunc ac tortor convallis mauris mattis volutpat. Nullam ullamcorper mi ut aliquet ullamcorper."

          setTimeout(() => {
            let extracted = document.createElement("div");
            extracted.classList.add("article__body-content", "article__body-content--added");
            extracted.id = "article-body-content"
            extracted.innerHTML = html;

            let content = document.getElementById("article-body-content");
            content.classList.add("article__body-content--removed");

            setTimeout(() => {
              content.replaceWith(extracted);
            }, 200);

            setTimeout(() => {
              let reloaded = document.getElementById("article-body-content");
              reloaded.classList.add("article__body-content--extracted");
              reloaded.classList.add("article__body-content--extracted");
            }, 400);
          }, 1_200);
        })();
      </script>
    SCRIPT

    Article.new(
      title: "This is Sonos’ next flagship soundbar",
      feed_name: "The Verge - All Posts",
      external_link: "https://www.theverge.com/2024/7/11/24195947/sonos-lasso-soundbar-photos-features-leak",
      byline: "July 11, 2024 at 7:05 AM by Chris Welch",
      body: "Some initial value here... Sed iaculis nec tellus sit amet volutpat. Cras id faucibus nulla, vel pharetra felis. Ut eu quam leo. Pellentesque egestas quam velit, eu interdum augue lobortis et. Quisque ex arcu, mollis nec cursus in, sagittis non tellus. Integer bibendum leo scelerisque, sollicitudin elit vel, lobortis lorem. In semper ex ut eleifend pellentesque. Nam sed lorem ante.",
      script:
    )
  end
end

require "./articles/factorio"
require "./articles/distrowatch"
require "./articles/tagesschau"
require "./articles/one_pezeshk"
require "./articles/secretclub"
