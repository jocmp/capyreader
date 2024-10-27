# frozen_string_literal: true

require "sass-embedded"

class StyleMinifier
  BASE_NAME = "stylesheet"

  def self.minify(destination: "public/assets")
    compressed_css = Sass.compile("style/#{BASE_NAME}.scss", style: :compressed).css

    File.write("#{destination}/#{File.basename("#{BASE_NAME}.css")}", compressed_css)
  end
end
