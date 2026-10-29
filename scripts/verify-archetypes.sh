#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GEN="${1:-/tmp/quanle-archetype-out}"
rm -rf "$GEN"
mkdir -p "$GEN"

generate() {
  local archetype="$1"
  local artifact="$2"
  local pkg="$3"
  local prefix="$4"
  mvn -B -N -f "$ROOT/pom.xml" archetype:generate \
    -DgroupId=com.example \
    -DartifactId="$artifact" \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackage="$pkg" \
    -Dauthor=ci \
    -DerrorCodePrefix="$prefix" \
    -DquanleParentVersion=1.0.0-SNAPSHOT \
    -DarchetypeGroupId=com.quanle \
    -DarchetypeArtifactId="$archetype" \
    -DarchetypeVersion=1.0.0-SNAPSHOT \
    -DoutputDirectory="$GEN" \
    -DinteractiveMode=false
  if grep -R -n '\${package}' "$GEN/$artifact" --include='AGENTS.md'; then
    echo "AGENTS.md still contains the package placeholder"
    exit 1
  fi
  if ! grep -q "$pkg" "$GEN/$artifact/AGENTS.md"; then
    echo "AGENTS.md did not receive the generated package"
    exit 1
  fi
  (cd "$GEN/$artifact" && mvn -B verify)
}

generate quanle-archetype-modular sample-modular com.example.modular MDL
generate quanle-archetype-single sample-single com.example.single SGL
echo "archetype projects verified in $GEN"
