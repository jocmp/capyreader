STYLE_OUT =  ./dist/stylesheet.css
STYLE_DEST ?= ../capy/src/main/assets/stylesheet.css
TEMPLATE_OUT = ./dist/template.html
TEMPLATE_DEST ?= ../capy/src/main/res/raw/template.html
CUSTOM_EXTRACTOR_JS_DEST := ../capy/src/main/assets/custom-extractors.js
MEDIA_LISTENERS_JS_DEST := ../capy/src/main/assets/media.js

SHELL:=/usr/bin/env bash

.PHONY: forge clean

build: $(TEMPLATE_DEST) $(STYLE_DEST) $(MEDIA_LISTENERS_JS_DEST) $(CUSTOM_EXTRACTOR_JS_DEST)

.PHONY: forge
forge:
	bundle install
	bundle exec rerun main.rb -b --ignore "*.css"

clean:
	rm -f dist/*

$(STYLE_OUT): style/stylesheet.scss
	ruby ./script/generate-android-style

$(STYLE_DEST): $(STYLE_OUT)
	cp $(STYLE_OUT) $(STYLE_DEST)

dist/template.html: ./views/template.liquid
	ruby ./script/generate-android-template

$(TEMPLATE_DEST): dist/template.html
	cp ./dist/template.html $(TEMPLATE_DEST)

$(CUSTOM_EXTRACTOR_JS_DEST): ./public/assets/custom-extractors.js
	cp ./public/assets/custom-extractors.js $(CUSTOM_EXTRACTOR_JS_DEST)

$(MEDIA_LISTENERS_JS_DEST): ./public/assets/media.js
	cp ./public/assets/media.js $(MEDIA_LISTENERS_JS_DEST)
