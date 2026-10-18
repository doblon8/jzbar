#!/usr/bin/env bash
# Extract the license texts of the pinned ZBar and libiconv tarballs into
# native/licenses/ and regenerate native/licenses/SHA256SUMS.
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
tar xjf zbar.tar.bz2
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

# checksums of everything under native/licenses
cd "$LIC"
list=$(find . -type f ! -name SHA256SUMS | sed 's|^\./||' | LC_ALL=C sort)
: > SHA256SUMS
while IFS= read -r f; do
  printf '%s  %s\n' "$(sha256_of "$f")" "$f" >> SHA256SUMS
done <<EOF
$list
EOF
echo "wrote native/licenses/SHA256SUMS ($(wc -l < SHA256SUMS | tr -d ' ') files)"
