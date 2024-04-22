SHELL:=/usr/bin/env bash

FASTLANE ?= bundle exec fastlane

.PHONY: test

test:
	$(FASTLANE) test
