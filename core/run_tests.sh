#!/bin/sh
# Host tests for the app's plain Kotlin core (no Android needed), with the tree's Kotlin compiler and
# JUnit. Soong runs the same tests as PcLoginCoreTests.
set -e
HERE=$(cd "$(dirname "$0")" && pwd)
TOP=${ANDROID_BUILD_TOP:-$(cd "$HERE/../../../.." && pwd)}
LIB=$TOP/external/kotlinc/lib
JUNIT=$TOP/prebuilts/devtools/tools/lib/junit-4.12.jar:$TOP/prebuilts/devtools/tools/lib/hamcrest-core-1.3.jar
OUT=$HERE/../build/core-tests
rm -rf "$OUT" && mkdir -p "$OUT"
# Keep the compiler small: the machine may be busy with an Android build.
JAVA_OPTS="-Xmx1g" "$TOP/external/kotlinc/bin/kotlinc" -nowarn -cp "$JUNIT" -d "$OUT" "$HERE/src" "$HERE/tests"
# Test classes come from the test sources, so a helper class never runs as a test.
TESTS=$(cd "$HERE/tests" && find . -name '*Test.kt' | sed 's|^\./||; s|\.kt$||; s|/|.|g' | sort)
java -Xmx256m -cp "$OUT:$JUNIT:$LIB/kotlin-stdlib.jar" org.junit.runner.JUnitCore $TESTS
