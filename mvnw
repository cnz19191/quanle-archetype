#!/bin/sh
# 最小化的 Maven Wrapper：固定 Maven 版本，并把本地仓库隔离到项目内 .mvn/repository。
# 不读取全局 M2_HOME / MAVEN_HOME。首次运行会下载 Maven 发行包。
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPS="$ROOT/.mvn/wrapper/maven-wrapper.properties"
[ -f "$PROPS" ] || { echo "缺少 $PROPS" >&2; exit 1; }

prop() {
    sed -n "s/^$1=//p" "$PROPS" | head -n 1
}

DIST_URL=$(prop distributionUrl)
DIST_SHA=$(prop distributionSha256Sum)
[ -n "$DIST_URL" ] || { echo "distributionUrl 未配置" >&2; exit 1; }

# 可选的项目内本地环境（不提交）. 可设置 LOCAL_MAVEN_HOME / LOCAL_MAVEN_REPO，
# 优先复用机器上已有的 Maven 和仓库，避免重复下载。
if [ -f "$ROOT/.mvn/local-env" ]; then
    . "$ROOT/.mvn/local-env"
fi
REPO="${LOCAL_MAVEN_REPO:-$ROOT/.mvn/repository}"

if [ -n "${LOCAL_MAVEN_HOME:-}" ] && [ -x "$LOCAL_MAVEN_HOME/bin/mvn" ]; then
    mkdir -p "$REPO"
    unset M2_HOME MAVEN_HOME
    exec "$LOCAL_MAVEN_HOME/bin/mvn" -Dmaven.repo.local="$REPO" "$@"
fi

CACHE="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists"
ARCHIVE=$(basename "$DIST_URL")
DIST_DIR="$CACHE/$(printf '%s' "$ARCHIVE" | sed 's/[^A-Za-z0-9._-]/_/g')"
mkdir -p "$DIST_DIR"

MVN_BIN=$(find "$DIST_DIR" -type f -name mvn -path '*/bin/mvn' 2>/dev/null | head -n 1 || true)
if [ -z "$MVN_BIN" ]; then
    TMP="$DIST_DIR/$ARCHIVE"
    echo "Downloading Maven from $DIST_URL" >&2
    if command -v curl >/dev/null 2>&1; then
        curl -fL --retry 3 --retry-delay 2 -o "$TMP" "$DIST_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$TMP" "$DIST_URL"
    else
        echo "需要 curl 或 wget 才能下载 Maven" >&2
        exit 1
    fi
    if [ -n "$DIST_SHA" ]; then
        echo "$DIST_SHA  $TMP" | shasum -a 256 -c - >&2
    fi
    case "$ARCHIVE" in
        *.zip) unzip -q -o "$TMP" -d "$DIST_DIR" ;;
        *.tar.gz|*.tgz) tar -xzf "$TMP" -C "$DIST_DIR" ;;
        *) echo "不支持的发行包格式：$ARCHIVE" >&2; exit 1 ;;
    esac
    rm -f "$TMP"
    MVN_BIN=$(find "$DIST_DIR" -type f -name mvn -path '*/bin/mvn' | head -n 1)
fi

[ -n "$MVN_BIN" ] || { echo "解压后找不到 bin/mvn" >&2; exit 1; }

mkdir -p "$REPO"
unset M2_HOME MAVEN_HOME
exec "$MVN_BIN" -Dmaven.repo.local="$REPO" "$@"
