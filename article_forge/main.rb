require "sinatra"
require "rack"
require "./color_picker"
require "./font_picker"
require "./style_minifier"
require "securerandom"
require "liquid"

js_context = nil

article_cache = {}

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
  article_content = article_cache[article_url] || JSON.parse(`cd ../../mercury-parser && npx mercury-parser #{article_url}`)

  article_cache[article_url] = article_content

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

get "/audio-enclosure" do
  colors = ColorPicker.pick(params["theme"])

  examples = [
    { "label" => "With artwork and duration", "title" => "Episode Title Here", "feed" => "Podcast Name", "duration" => "52:02", "artwork" => "https://picsum.photos/200", "playing" => false },
    { "label" => "With artwork, no duration", "title" => "Another Episode", "feed" => "Another Podcast", "duration" => nil, "artwork" => "https://picsum.photos/201", "playing" => false },
    { "label" => "Without artwork (placeholder)", "title" => "No Artwork Episode", "feed" => "Minimal Podcast", "duration" => "2:02:35", "artwork" => nil, "playing" => false },
    { "label" => "Long title (truncation test)", "title" => "This Is A Very Long Episode Title That Should Be Truncated At Some Point", "feed" => "The Extremely Long Podcast Name That Goes On And On", "duration" => "30:00", "artwork" => "https://picsum.photos/202", "playing" => false },
    { "label" => "Playing state", "title" => "Currently Playing Episode", "feed" => "Active Podcast", "duration" => "45:30", "artwork" => "https://picsum.photos/203", "playing" => true },
    { "label" => "Very long title", "title" => "Episode 247: Why Do We Dream? The Science Behind Sleep, REM Cycles, and What Your Subconscious Might Be Trying to Tell You About Your Waking Life", "feed" => "The Extremely Curious Science Podcast With Dr. Jane Smith and Professor John Doe", "duration" => "1:42:15", "artwork" => "https://picsum.photos/204", "playing" => false },
  ]

  liquid :"audio-enclosure", locals: {
    font_size: params["font_size"] || "16px",
    title_font_size: params["title_font_size"] || "1.5em",
    examples: examples,
  }.merge(colors)
end

def published(timestamp)
  return "" if !timestamp

  published = Time.parse(timestamp).strftime("%B %-d, %Y at %I:%M %p")

  "on #{published}"
end
