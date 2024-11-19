require "json"

module Articles
  class << self
    def andreyor
      body = File.read("articles/fixtures/andreyor.html")

      Article.new(
        title: "Making Emacs tabs look like in Atom",
        feed_name: "Andrey Listopadov",
        external_link: "https://andreyor.st/posts/2020-05-10-making-emacs-tabs-look-like-in-atom/",
        body:,
      )
    end
  end
end
