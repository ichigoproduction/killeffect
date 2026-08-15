#!/usr/bin/env sh
set -eu

GRADLE_VERSION=9.5.1
PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BOOTSTRAP_DIR="$PROJECT_DIR/.gradle-bootstrap"
GRADLE_HOME="$BOOTSTRAP_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"
GRADLE_ZIP="$BOOTSTRAP_DIR/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_BIN" ]; then
  echo "[killeffect] Gradle $GRADLE_VERSION not found. Downloading it once..."
  mkdir -p "$BOOTSTRAP_DIR"

  if command -v curl >/dev/null 2>&1; then
    curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$GRADLE_ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$GRADLE_ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  else
    echo "[killeffect] curl or wget is required to bootstrap Gradle." >&2
    exit 1
  fi

  if ! command -v unzip >/dev/null 2>&1; then
    echo "[killeffect] unzip is required to bootstrap Gradle." >&2
    exit 1
  fi

  unzip -q -o "$GRADLE_ZIP" -d "$BOOTSTRAP_DIR"
  rm -f "$GRADLE_ZIP"
fi

exec "$GRADLE_BIN" "$@"
