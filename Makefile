.PHONY: release pre-release

release:
	bumpver update --verbose

pre-release:
	bumpver update --verbose --tag=beta
