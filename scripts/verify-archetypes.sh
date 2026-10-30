#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GEN="${1:-/tmp/isiniu-archetype-out}"
REPO="$ROOT/.mvn/repository"
if [ -f "$ROOT/.mvn/local-env" ]; then
  . "$ROOT/.mvn/local-env"
fi
REPO="${LOCAL_MAVEN_REPO:-$REPO}"
if [ -x "$ROOT/mvnw" ]; then
  MVN="$ROOT/mvnw"
else
  MVN="mvn"
fi
rm -rf "$GEN"
mkdir -p "$GEN"

generate() {
  local archetype="$1"
  local artifact="$2"
  local pkg="$3"
  local prefix="$4"
  "$MVN" -B -N -f "$ROOT/pom.xml" -Dmaven.repo.local="$REPO" archetype:generate \
    -DgroupId=com.example \
    -DartifactId="$artifact" \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackage="$pkg" \
    -Dauthor=ci \
    -DerrorCodePrefix="$prefix" \
    -DisiniuParentVersion=1.0.0-SNAPSHOT \
    -DarchetypeGroupId=com.isiniu \
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
  (cd "$GEN/$artifact" && "$MVN" -B -Dmaven.repo.local="$REPO" verify)
}

generate isiniu-archetype-modular sample-modular com.example.modular MDL
generate isiniu-archetype-single sample-single com.example.single SGL
echo "archetype projects verified in $GEN"
