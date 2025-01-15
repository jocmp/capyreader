require "json"

module Articles
  class << self
    def versants
      extracted = File.read("articles/fixtures/versants.html")

      Article.new(
        title: "Élections fédérales : les visites de Justin Trudeau à Saint-Bruno",
        feed_name: "versants.com",
        external_link: "https://www.versants.com/actualite/elections-federales-les-visites-de-justin-trudeau-a-saint-bruno/",
        byline: nil,
        body: extracted
      )
    end
  end
end
