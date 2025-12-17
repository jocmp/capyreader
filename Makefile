SHELL:=/usr/bin/env bash

FASTLANE ?= bundle exec fastlane

.PHONY: test release-secrets deploy-production

.PHONY: assets
assets:
	$(MAKE) -C article_forge clean build

.PHONY: tsc
tsc: ## Type-check JavaScript files
	npx -p typescript tsc

.PHONY: deps
deps: ## Install bumpver
	pip install bumpver==2024.1130

.PHONY: bump-release-dev
bump-release-dev: ## Bump GitHub version
	bumpver update --tag=dev --push

.PHONY: bump-release-production
bump-release-production: ## Bump Google Play version
	bumpver update --tag=final

.PHONY: changelog
changelog: ## Prep next release notes
	./scripts/changelog

test: ## Run all tests
	$(FASTLANE) test

deploy-production: release-secrets
	$(FASTLANE) production

deploy-github-release: shared-release-secrets
	$(FASTLANE) github_release

deploy-free-nightly: shared-release-secrets
	$(FASTLANE) free_nightly

.SILENT:
release-secrets: shared-release-secrets
	echo ${ENCODED_GOOGLE_PLAY_CREDENTIALS} | base64 --decode > ./google-play-service-account.json

.SILENT:
shared-release-secrets:
	echo ${ENCODED_GOOGLE_SERVICES} | base64 --decode > ./app/google-services.json
	echo ${ENCODED_RELEASE_KEYSTORE} | base64 --decode > ./release.keystore
	echo ${ENCODED_SECRETS_PROPERTIES} | base64 --decode > ./secrets.properties

.PHONY: help
help:
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' Makefile | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
