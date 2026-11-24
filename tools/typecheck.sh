#!/bin/bash
# Compiles the app (core + Compose UI) on the host with the tree's Kotlin compiler and Compose plugin,
# against the prebuilt androidx jars and the framework and coroutines jars of a previous build
# (out/soong/.intermediates). A quick check before a full build; it builds no APK.
#   tools/typecheck.sh [WORK_DIR]
set -e
HERE=$(cd "$(dirname "$0")/.." && pwd)
TOP=${ANDROID_BUILD_TOP:-$(cd "$HERE/../../.." && pwd)}
WORK=${1:-$HERE/build/typecheck}
M=$TOP/prebuilts/sdk/current/androidx/m2repository/androidx
INTER=$TOP/out/soong/.intermediates
rm -rf "$WORK" && mkdir -p "$WORK/libs" "$WORK/gen"

for a in $(cd "$M" && ls -d compose/*/* lifecycle/* activity/* savedstate/* navigationevent/* core/core \
           collection/collection-jvm annotation/annotation-jvm annotation/annotation-experimental); do
  v=$(ls "$M/$a" | sort -V | tail -1)
  for f in "$M/$a/$v"/*; do
    case "$f" in
      *sources*) ;;
      *.aar) unzip -p "$f" classes.jar > "$WORK/libs/$(echo "$a" | tr / _).jar" 2>/dev/null || true ;;
      *.jar) cp "$f" "$WORK/libs/" ;;
    esac
  done
done

# R with the resource ids the code uses (strings, bools, dimens of res/values).
python3 - "$HERE/res/values" "$WORK/gen/R.kt" <<'PY'
import sys, pathlib, xml.etree.ElementTree as ET
kinds = {}
for xml in sorted(pathlib.Path(sys.argv[1]).glob("*.xml")):
    for e in ET.parse(xml).getroot():
        if e.tag in ("string", "bool", "dimen"):
            kinds.setdefault(e.tag, []).append(e.get("name"))
blocks = []
for n, (kind, names) in enumerate(sorted(kinds.items())):
    ids = "\n".join(f"        const val {name} = {n * 1000 + i + 1}" for i, name in enumerate(names))
    blocks.append(f"    object {kind} {{\n{ids}\n    }}")
body = "\n".join(blocks)
open(sys.argv[2], "w").write(f"package org.lineageos.pclogin\n\nobject R {{\n{body}\n}}\n")
PY

CP=$(ls "$WORK"/libs/*.jar | tr '\n' ':')
CP=$CP$INTER/external/kotlinx.coroutines/kotlinx_coroutines/linux_glibc_common/combined/kotlinx_coroutines.jar
CP=$CP:$INTER/frameworks/base/framework-minus-apex/android_common/combined/framework.jar
JAVA_OPTS="-Xmx1536m" nice -n 19 "$TOP/external/kotlinc/bin/kotlinc" -no-reflect -jvm-target 21 \
  -Xplugin="$TOP/external/kotlinc/lib/compose-compiler-plugin.jar" \
  -cp "$CP" -d "$WORK/out" "$HERE/core/src" "$HERE/src" "$WORK/gen"
echo "typecheck: ok"
