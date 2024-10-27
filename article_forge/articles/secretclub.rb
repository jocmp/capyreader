require "json"

module Articles
  class << self
    def secretclub
      extracted = File.read("articles/fixtures/secretclub.html")

      Article.new(
        title: "Ring Around The Regex: Lessons learned from fuzzing regex libraries (Part 1)",
        feed_name: "secret club",
        external_link: "https://secret.club/2024/06/30/ring-around-the-regex-1.html",
        byline: "June 30, 2024 at 5:00 PM by addison",
        body: extracted
      )
    end
  end
end
