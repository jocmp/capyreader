
STYLE_OUT =  ./dist/stylesheet.css
STYLE_RES ?= ../capy/src/main/assets/stylesheet.css
TEMPLATE_OUT = ./dist/template.html
TEMPLATE_RES ?= ../capy/src/main/res/raw/template.html
MEDIA_LISTENERS_JS_SRC = ./public/assets/media.js
FULL_CONTENT_JS_SRC = ./public/assets/full-content.js
MEDIA_LISTENERS_JS_RES := ../capy/src/main/assets/media.js
MERCURY_JS_RES = ../capy/src/main/assets/mercury-parser.js
FULL_CONTENT_JS_RES = ../capy/src/main/assets/full-content.js

SHELL:=/usr/bin/env bash

.PHONY: forge clean

build: $(TEMPLATE_RES) $(FULL_CONTENT_JS_RES) $(STYLE_RES) $(MERCURY_JS_RES) $(MEDIA_LISTENERS_JS_RES)

$(STYLE_OUT): style/stylesheet.scss
	@echo "Style..."
	ruby ./script/generate-android-style

$(STYLE_RES): $(STYLE_OUT)
	cp $(STYLE_OUT) $(STYLE_RES)

$(TEMPLATE_OUT): ./views/template.liquid
	@echo "Template..."
	ruby ./script/generate-android-template

$(TEMPLATE_RES): $(TEMPLATE_OUT)
	cp $(TEMPLATE_OUT) $(TEMPLATE_RES)

$(MEDIA_LISTENERS_JS_RES): $(MEDIA_LISTENERS_JS_SRC)
	@echo 'Media script...'
	cp $(MEDIA_LISTENERS_JS_SRC) $(MEDIA_LISTENERS_JS_RES)

$(FULL_CONTENT_JS_RES): $(FULL_CONTENT_JS_SRC)
	@echo 'Full content script...'
	cp $(FULL_CONTENT_JS_SRC) $(FULL_CONTENT_JS_RES)

.PHONY: $(MERCURY_JS_RES)
$(MERCURY_JS_RES):
	@echo 'Mercury script...'
	cp ../../mercury-parser/dist/mercury.web.js $(MERCURY_JS_RES)

.PHONY: build-mercury
build-mercury:
	cd ../../mercury-parser && yarn build yarn build:web

.PHONY: forge
forge:
	bundle install
	bundle exec rerun main.rb -b --ignore "*.css"

clean:
	rm -f dist/*

