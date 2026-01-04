# frozen_string_literal: true

require "sass-embedded"

class StyleMinifier
  BASE_NAME = "stylesheet"

  def self.minify(destination: "public/assets")
    compressed_css = Sass.compile("style/#{BASE_NAME}.scss", style: :compressed).css

    # Local version keeps /assets/ paths for development
    File.write("#{destination}/#{File.basename("#{BASE_NAME}.css")}", compressed_css)

    # Android version with appassets URLs
    android_css = compressed_css.gsub('url("/', 'url("https://appassets.androidplatform.net/')
    File.write("dist/#{File.basename("#{BASE_NAME}.css")}", android_css)
  end
end
