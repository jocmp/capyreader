package com.jocmp.rssparser.atom

import com.jocmp.rssparser.BaseParserTest

class XmlParserAtomRelativeImageTest : BaseParserTest(
    feedPath = "atom-relative-image.xml",
    channelTitle = "Crystal's Wobsite: Articles",
    channelLink = "https://crystalwobsite.gay",
    channelLastBuildDate = "2025-10-03T00:00:00Z",
    articleTitle = "Working on Fluentflame Reader",
    articleLink = "https://crystalwobsite.gay/posts/2025-10-03-fluentflame",
    articleGuid = "https://crystalwobsite.gay/posts/2025-10-03-fluentflame.html",
    articlePubDate = "2025-10-03T00:00:00+00:00",
    articleImage = "https://crystalwobsite.gay/images/post_images/2025-10-03-fluentflame.svg",
    articleDescription = """Small blogpost because I don’t feel like writing a long one, but I felt I
should talk a little about what I’ve been working on in my free time.


Fluentflame Reader!

Wow! It’s an RSS
reader.
If you read my blogpost about RSS
readers, you may recall there was a reader
called Fluent Reader. Fluent Reader, to my knowledge, is abandoned. The last
update was 8 months ago, and even some very very basic security updates have
not been made in years.
When I tried to send over
patches, they were met
with crickets.
SO.
I helped make a hardfork! Now we have Fluentflame Reader, the attempted
successor to Fluent Reader. It has a number of bug fixes already, a few tiny
features added, and some code rewrites. I intend to keep working on this until
it becomes an RSS reader me and my friends actually want to use.
❦

  You can email me at “crystal (at) <this domain>” to respond! Or just say hi. That's cool too!""",
    articleContent = """<p>Small blogpost because I don’t feel like writing a long one, but I felt I
should talk a little about what I’ve been working on in my free time.</p>
<figure>
<img src="../images/post_images/2025-10-03-fluentflame.svg" alt="Fluentflame Reader!" />
<figcaption aria-hidden="true">Fluentflame Reader!</figcaption>
</figure>
<p><strong>Wow! <a href="https://github.com/FluentFlame/fluentflame-reader">It’s an RSS
reader.</a></strong></p>
<p>If you read my <a href="../posts/2024-09-12-rss_reviews">blogpost about RSS
readers</a>, you may recall there was a reader
called Fluent Reader. Fluent Reader, to my knowledge, is abandoned. The last
update was 8 months ago, and even some very very basic security updates have
not been made in years.</p>
<p>When I tried to send over
<a href="https://github.com/yang991178/fluent-reader/pull/733">patches</a>, they were met
with crickets.</p>
<p>SO.</p>
<p>I helped make a hardfork! Now we have Fluentflame Reader, the attempted
successor to Fluent Reader. It has a number of bug fixes already, a few tiny
features added, and some code rewrites. I intend to keep working on this until
it becomes an RSS reader me and my friends actually want to use.</p>
<div class="post-contact-spacer">❦</div>
<div class="post-contact">
  You can email me at <email-wrapper>“crystal (at) &lt;this domain&gt;”</email-wrapper> to respond!<br /> Or just say hi. That's cool too!
</div>""",
)
