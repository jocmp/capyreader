SHELL:=/usr/bin/env bash

FASTLANE ?= bundle exec fastlane

test:
	$(FASTLANE) test
