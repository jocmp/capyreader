SHELL:=/usr/bin/env bash

FASTLANE ?= bundle exec fastlane

.PHONY: test release-secrets deploy-production

.PHONY: deps
deps:
	bundle install
	pip install bumpver==2023.1129

.PHONY: changelog
changelog:
	./scripts/changelog

test:
	$(FASTLANE) test

deploy-production: release-secrets
	$(FASTLANE) production

.SILENT:
release-secrets:
	echo ${ENCODED_GOOGLE_PLAY_CREDENTIALS} | base64 --decode > ./google-play-service-account.json
	echo ${ENCODED_GOOGLE_SERVICES} | base64 --decode > ./app/google-services.json
	echo ${ENCODED_RELEASE_KEYSTORE} | base64 --decode > ./release.keystore
	echo ${ENCODED_SECRETS_PROPERTIES} | base64 --decode > ./secrets.properties
