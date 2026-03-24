# frozen_string_literal: true

require "sinatra"
require "sass-embedded"
require "liquid"

PORT = 8484

set :port, PORT
set :bind, "0.0.0.0"

ASSETS_DIR = File.expand_path("../app/src/main/assets", __dir__)
FONT_DIR = File.expand_path("../app/src/main/res/font", __dir__)
STYLE_DIR = File.join(__dir__, "style")
VIEWS_DIR = File.join(__dir__, "views")

# Compile SCSS to CSS (expanded, not compressed, for dev readability)
def compile_css
  css = Sass.compile(File.join(STYLE_DIR, "stylesheet.scss"), style: :expanded).css
  # In dev mode, rewrite font URLs to point at our local server instead of appassets
  css
end

# Render the Liquid template with placeholder values for development
def render_template
  source = File.read(File.join(VIEWS_DIR, "template.liquid"))

  debug_script = %(<script>document.write('<script src="http://' + (location.host || 'localhost:#{PORT}') + '/reload.js"><\\/script>')</script>)

  template = Liquid::Template.parse(source)
  template.render(
    "color_primary" => "#6750A4",
    "color_surface" => "#FFFBFE",
    "color_surface_container_highest" => "#E6E1E5",
    "color_on_surface" => "#1C1B1F",
    "color_on_surface_variant" => "#49454F",
    "color_surface_variant" => "#E7E0EC",
    "color_primary_container" => "#EADDFF",
    "color_on_primary_container" => "#21005D",
    "color_secondary" => "#625B71",
    "color_surface_container" => "#F3EDF7",
    "color_surface_tint" => "#6750A4",
    "font_size" => "1.0rem",
    "title_font_size" => "1.5rem",
    "title_text_align" => "start",
    "pre_white_space" => "pre-wrap",
    "table_overflow_x" => "auto",
    "debug_script" => debug_script,
    "font_preload" => "",
    "external_link" => "#",
    "title" => "Article Title",
    "byline" => "By Author",
    "feed_name" => "Feed Name",
    "body" => "<p>Article body content goes here. Edit <code>views/template.liquid</code> or <code>style/stylesheet.scss</code> and the page will reload.</p>",
    "font_family" => "default",
    "title_font_family" => "default"
  )
end

# --- Routes ---

# Serve compiled CSS
get "/assets/stylesheet.css" do
  content_type "text/css"
  compile_css
end

# Serve JS files from Android assets
get "/assets/:filename" do |filename|
  file = File.join(ASSETS_DIR, filename)
  pass unless File.exist?(file)

  case File.extname(filename)
  when ".js"  then content_type "application/javascript"
  when ".svg" then content_type "image/svg+xml"
  else content_type "application/octet-stream"
  end

  send_file file
end

# Serve font files
get "/res/font/:filename" do |filename|
  file = File.join(FONT_DIR, filename)
  pass unless File.exist?(file)
  content_type "font/ttf"
  send_file file
end

# Simple auto-reload script (polls for changes)
get "/reload.js" do
  content_type "application/javascript"
  <<~JS
    (function() {
      var lastETag = null;
      setInterval(function() {
        fetch('//__reload_check')
          .then(function(r) {
            var etag = r.headers.get('etag');
            if (lastETag && etag !== lastETag) {
              location.reload();
            }
            lastETag = etag;
          })
          .catch(function() {});
      }, 1000);
    })();
  JS
end

# Reload check endpoint - returns an ETag based on file modification times
get "/__reload_check" do
  scss_files = Dir.glob(File.join(STYLE_DIR, "**/*.scss"))
  liquid_files = Dir.glob(File.join(VIEWS_DIR, "**/*.liquid"))
  all_files = scss_files + liquid_files

  mtime_hash = all_files.sort.map { |f| "#{f}:#{File.mtime(f).to_i}" }.join("|")
  etag Digest::MD5.hexdigest(mtime_hash)
  ""
end

# Root serves the rendered template
get "/" do
  content_type "text/html"
  render_template
end

puts ""
puts "=== Capy Reader Asset Forge ==="
puts "Local:    http://localhost:#{PORT}"
puts "Emulator: http://10.0.2.2:#{PORT}"
puts ""
