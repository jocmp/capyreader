# frozen_string_literal: true

require "sass-embedded"

class StyleMinifier
  BASE_NAME = "stylesheet"

  def self.minify
    compressed_css = Sass.compile("style/#{BASE_NAME}.scss", style: :compressed).css
    android_css = compressed_css.gsub('url("/', 'url("https://appassets.androidplatform.net/')
    File.write("dist/#{BASE_NAME}.css", android_css)
  end
end
