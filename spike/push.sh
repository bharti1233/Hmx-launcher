#!/usr/bin/env bash
# Pushes the current HEAD of the spike workspace to the Phase-1 GitHub repository.
# The PAT must be supplied via the GITHUB_PAT environment variable; it is consumed and never
# printed, committed, or stored inside any tracked file. No remote is registered, so the token
# never persists in .git/config either.
set -euo pipefail

: "${GITHUB_PAT:?Set GITHUB_PAT before calling push.sh (token is consumed, never echoed)}"

URL="https://bharti1233:${GITHUB_PAT}@github.com/bharti1233/Hmx-launcher.git"

git push "$URL" HEAD:main
