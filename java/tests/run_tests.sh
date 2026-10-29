#!/bin/bash
#
# Runs InvariantCheck, then every command file and bad input through
# TestBankers. See tests/README.md for expected results.

RESOURCES="10 6 7 8"

TESTS_DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_DIR="$(dirname "$TESTS_DIR")"
OUTPUT_DIR="$TESTS_DIR/output"

# Compile into a temporary folder so no .class files land in the repo
BUILD_DIR="$(mktemp -d)"
trap 'rm -rf "$BUILD_DIR"' EXIT

cd "$JAVA_DIR" || exit 1
javac -d "$BUILD_DIR" BankInterface.java BankImpl.java TestBankers.java tests/InvariantCheck.java \
    || { echo "Compile failed"; exit 1; }

# Stops a run after 10 seconds in case the command loop never exits
run_limited() {
    perl -e 'alarm 10; exec @ARGV' "$@"
}

echo "=================================================="
echo " InvariantCheck"
echo "=================================================="
java -cp "$BUILD_DIR" InvariantCheck
invariant_status=$?

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Command sequences for Tests 1-8 and the input edge cases
for commands in "$TESTS_DIR"/commands/*.txt; do
    name="$(basename "$commands" .txt)"
    {
        echo "\$ java TestBankers inPutFile.txt $RESOURCES < commands/$name.txt"
        echo
        run_limited java -cp "$BUILD_DIR" TestBankers inPutFile.txt $RESOURCES < "$commands" 2>&1
        echo
        echo "[exit status $?]"
    } > "$OUTPUT_DIR/$name.out"
done

# Bad input files and bad command-line arguments
{
    for bad_file in "$TESTS_DIR"/bad-files/*.txt; do
        echo "\$ java TestBankers bad-files/$(basename "$bad_file") $RESOURCES"
        run_limited java -cp "$BUILD_DIR" TestBankers "$bad_file" $RESOURCES < /dev/null 2>&1
        echo "[exit status $?]"
        echo
    done

    for args in "missing_file.txt $RESOURCES" \
                "inPutFile.txt" \
                "inPutFile.txt 10 6 7" \
                "inPutFile.txt 10 6 7 8 9" \
                "inPutFile.txt 10 -6 7 8" \
                "inPutFile.txt 10 six 7 8"; do
        echo "\$ java TestBankers $args"
        run_limited java -cp "$BUILD_DIR" TestBankers $args < /dev/null 2>&1
        echo "[exit status $?]"
        echo
    done
} > "$OUTPUT_DIR/bad_input.out"

echo
echo "TestBankers outputs saved in $OUTPUT_DIR"
echo "Compare them against the expected results in tests/README.md"

exit $invariant_status
