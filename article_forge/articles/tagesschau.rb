require "json"

module Articles
  class << self
    def tagesschau
      extracted = File.read("articles/fixtures/tagesschau.html")

      Article.new(
        title: "tagesschau in 100 Sekunden",
        feed_name: "tagesschau.de",
        external_link: "https://www.tagesschau.de/multimedia/sendung/tagesschau_in_100_sekunden/video-1371600.html",
        byline: nil,
        body: extracted
      )
    end
  end
end
