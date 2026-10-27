#!/usr/bin/env bash
# Extract the license texts of the pinned ZBar and libiconv tarballs into
# native/licenses/ and regenerate native/licenses/SHA256SUMS.
#
# SHA256SUMS covers every file in native/licenses/ that git knows about
# (tracked, or untracked but not ignored), including the texts added by hand.
# Needs git, curl and tar.
#
# Usage: native/extract-licenses.sh
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
load_sources ZBAR_VERSION ZBAR_SHA256 ICONV_VERSION ICONV_SHA256

LIC=$NATIVE_DIR/licenses
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

cd "$work"
fetch_zbar
fetch_iconv
tar xzf zbar.tar.gz
tar xzf libiconv.tar.gz

rm -rf "$LIC/zbar" "$LIC/libiconv"
mkdir -p "$LIC/zbar" "$LIC/libiconv"

# ZBar: copyright and exception notice, plus the LGPL-2.1 text
cp "zbar-$ZBAR_VERSION/COPYING" "$LIC/zbar/COPYING"
cp "zbar-$ZBAR_VERSION/LICENSE.md" "$LIC/zbar/LICENSE.md"

# libiconv: the library is LGPL-2.1. The top-level GPLv3 COPYING covers the
# iconv program, which is not distributed.
cp "libiconv-$ICONV_VERSION/COPYING.LIB" "$LIC/libiconv/COPYING.LIB"
# libcharset ships the same text; fail if an upgrade makes it differ
cmp "libiconv-$ICONV_VERSION/COPYING.LIB" "libiconv-$ICONV_VERSION/libcharset/COPYING.LIB"

# Checksums of everything under native/licenses that git knows about, so editor
# backup files and other ignored files never end up in SHA256SUMS.
cd "$LIC"
list=$(git ls-files --cached --others --exclude-standard -- . | { grep -vx SHA256SUMS || true; } | LC_ALL=C sort)
[ -n "$list" ] || { echo "FAIL: no license files found in native/licenses" >&2; exit 1; }

: > SHA256SUMS
while IFS= read -r f; do
  printf '%s  %s\n' "$(sha256_of "$f")" "$f" >> SHA256SUMS
done <<EOF
$list
EOF
echo "wrote native/licenses/SHA256SUMS ($(wc -l < SHA256SUMS | tr -d ' ') files)"
