.PHONY: release pre-release

release:
	bumpver update --verbose --pin-increments --tag=final

pre-release:
	bumpver update --verbose --tag=beta
