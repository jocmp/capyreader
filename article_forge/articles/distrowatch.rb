require "json"

module Articles
  class << self
    def distrowatch
      extracted = File.read("articles/fixtures/distrowatch.html")

      Article.new(
        title: "DistroWatch Weekly, Issue 1084",
        feed_name: "DistroWatch Weekly",
        external_link: "https://www.factorio.com/blog/post/fff-421",
        byline: nil,
        body: extracted
      )
    end
  end
end
