require "json"

module Articles
  class << self
    def factorio
      extracted = JSON.parse(File.read("articles/fixtures/factorio.json"))

      Article.new(
        title: extracted["title"],
        feed_name: "Factorio",
        external_link: "https://www.factorio.com/blog/post/fff-421",
        byline: extracted["author"],
        body: extracted["content"]
      )
    end
  end
end
