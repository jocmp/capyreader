require "sinatra"
require "rack"
require "./articles"
require "./color_picker"
require "./font_picker"
require "./style_minifier"
require "securerandom"
require "liquid"

configure do
  Sinatra::Application.reset!
  StyleMinifier.minify
end

get "/" do
  redirect to("/articles/nine_to_five_google")
end

get "/articles/:slug" do
  article = Articles.find(params["slug"])

  colors = ColorPicker.pick(params["theme"])

  font_family = FontPicker.pick(params["font_family"])

  liquid :template, locals: {
    title: article.title,
    byline: article.byline,
    external_link: article.external_link,
    feed_name: article.feed_name,
    body: article.body,
    text_size: params["text_size"],
    font_family:,
    assets_url: "",
    resource_url: ""
  }.merge(colors)
end

get "/test-room" do
  colors = ColorPicker.pick(params["theme"])

  liquid :"test-room", locals: {
    unvisited_link: "https://example.com/#{SecureRandom.uuid}"
  }.merge(colors)
end
