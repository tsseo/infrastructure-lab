#!/usr/bin/env bash
# Pinpoint 에이전트 3.1.0 다운로드 & 압축 해제
# 실행: bash docker/pinpoint/download-agent.sh
# 결과: docker/pinpoint/pinpoint-agent/ 디렉터리 생성

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AGENT_DIR="$SCRIPT_DIR/pinpoint-agent"
VERSION="3.1.0"
TARBALL="pinpoint-agent-${VERSION}.tar.gz"
URL="https://github.com/pinpoint-apm/pinpoint/releases/download/v${VERSION}/${TARBALL}"

if [ -d "$AGENT_DIR" ]; then
  echo "[INFO] $AGENT_DIR 이미 존재. 건너뜀."
  exit 0
fi

echo "[INFO] 에이전트 다운로드 중: $URL"
curl -L -o "/tmp/${TARBALL}" "$URL"

echo "[INFO] 압축 해제 중: $AGENT_DIR"
mkdir -p "$AGENT_DIR"
tar -xzf "/tmp/${TARBALL}" -C "$AGENT_DIR" --strip-components=1

echo "[INFO] 완료: $AGENT_DIR"
ls "$AGENT_DIR"
