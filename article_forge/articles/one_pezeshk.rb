require "json"

module Articles
  class << self
    def one_pezeshk
      body = File.read("articles/fixtures/one_pezeshk.html")

      Article.new(
        title: "لپ‌تاپ جدید لنوو با هوش مصنوعی حرکات کاربر را دنبال می‌کند و جهت صفحه نمایش را به صورت خودکار تطبیق می‌دهد",
        feed_name: "1pezeshk",
        external_link: "https://www.1pezeshk.com/archives/2024/09/lenovo-made-a-laptop-that-can-transform-itself.html",
        byline: "علیرضا مجیدی",
        body:
      )
    end
  end
end
