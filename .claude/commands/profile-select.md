Run the bench select-profile command and format the output as a table.

1. Ensure bench data exists (if not, run `make bench-reset` then `./gradlew :bench:run --args="refresh"`)
2. Run `./gradlew :bench:run --args="select-profile"`
3. Parse the output and present the timing results in a markdown table
