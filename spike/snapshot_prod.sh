#!/usr/bin/env bash
# Byte-level snapshot of the production workspace: full file tree (type, mode, size, mtime)
# and per-file SHA-256 hashes. Run before the spike work and after it; diff the two to prove
# the production workspace stayed untouched.
#
# Usage: snapshot_prod.sh <output-dir>
set -eu

SRC=/mnt/sdcard/AIProjects/HMXLAUNCHER
OUT=$1
mkdir -p "$OUT"

# Directory/file entries with marker file so an empty dir has evidence too.
find "$SRC" -mindepth 0 -printf '%y %m %s %TY-%Tm-%Td %TH:%TM:%TS %P\n' 2>/dev/null \
  | sort > "$OUT/prod-tree.txt"

# Per-file hashes (paths are quoted by sha256sum when needed; none have spaces today).
find "$SRC" -type f -exec sha256sum {} + 2>/dev/null | sort > "$OUT/prod-hashes.txt"

echo "snapshot written:"
wc -l "$OUT/prod-tree.txt" "$OUT/prod-hashes.txt"
