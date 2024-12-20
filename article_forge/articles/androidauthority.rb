require "json"

module Articles
  class << self
    def androidauthority
      extracted = File.read("articles/fixtures/androidauthority.html")

      Article.new(
        title: "Samsung Galaxy S25 series rumors and leaks: Everything we know so far",
        feed_name: "Android Authority",
        external_link: "https://www.androidauthority.com/samsung-galaxy-s25-3437280/",
        byline: nil,
        body: extracted
      )
    end
  end
end
