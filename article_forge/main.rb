require "sinatra"
require "rack"
require "./color_picker"
require "./font_picker"
require "./style_minifier"
require "securerandom"
require "liquid"

js_context = nil

configure do
  Sinatra::Application.reset!
  StyleMinifier.minify
end

get "/" do
  colors = ColorPicker.pick(params["theme"])

  liquid :article_form, locals: colors
end

get "/articles" do
  colors = ColorPicker.pick(params["theme"])

  font_family = FontPicker.pick(params["font_family"])

  article_url = params["article_url"]
  article_content = JSON.parse(`cd ../../mercury-parser && npx mercury-parser #{article_url}`)

  liquid :template, locals: {
    title: article_content["title"],
    byline: "#{article_content["author"]}#{published(article_content["date_published"])}",
    external_link: article_url,
    feed_name: article_content["domain"],
    body: article_content["content"],
    text_size: params["text_size"],
    font_family:,
    debug_script: '<script type="text/javascript" src="/assets/debug.js"></script>',
    font_size: "16px",
  }.merge(colors)
end

get "/image" do
  image_src = Base64.decode64(params["src"])
  image_caption = params["caption"].to_s

  liquid :image, locals: {
    image_src:,
    image_caption:,
  }
end

get "/test-room" do
  colors = ColorPicker.pick(params["theme"])

  liquid :"test-room", locals: {
    unvisited_link: "https://example.com/#{SecureRandom.uuid}"
  }.merge(colors)
end

def published(timestamp)
  return "" if !timestamp

  published = Time.parse(article_content["date_published"]).strftime("%B %-d, %Y at %I:%M %p")

  "on #{published}"
end
