#!/usr/bin/env bash
# Generate THIRD-PARTY-NOTICES from native/manifest/*.manifest.
# The output depends only on the manifests, so CI can regenerate it and
# fail on any difference.
#
# Usage: native/make-notices.sh
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
export LC_ALL=C

manifests=("$NATIVE_DIR"/manifest/*.manifest)
[ -e "${manifests[0]}" ] || { echo "FAIL: no manifests found" >&2; exit 1; }

# values KEY: distinct values of KEY across all manifests, one per line.
values() {
  local f key=${1//./\\.}
  for f in "${manifests[@]}"; do
    tr -d '\r' < "$f" | sed -n "s/^$key=//p"
  done | sort -u
}

has() { [ -n "$(values "$1")" ]; }

# one KEY: the single value of KEY; fails if the manifests disagree.
one() {
  local v n
  v=$(values "$1")
  n=$(printf '%s\n' "$v" | grep -c . || true)
  [ "$n" = 1 ] || { echo "FAIL: $1 has $n distinct values in the manifests" >&2; exit 1; }
  printf '%s' "$v"
}

# expect KEY VALUE: the text below was written for this value. If it changes,
# review the matching paragraph, then update the value here.
expect() {
  local v
  v=$(one "$1")
  [ "$v" = "$2" ] || { echo "FAIL: $1 is '$v', expected '$2': review $0" >&2; exit 1; }
}

# files_of PREFIX: binaries whose component starts with PREFIX, comma-separated.
files_of() {
  local f
  for f in "${manifests[@]}"; do
    tr -d '\r' < "$f" | awk -F= -v p="$1" '
      $1 ~ /^file\.[0-9]+\.component$/ { split($1, a, "."); c[a[2]] = substr($0, index($0, "=") + 1) }
      $1 ~ /^file\.[0-9]+\.path$/      { split($1, a, "."); q[a[2]] = substr($0, index($0, "=") + 1) }
      END { for (n in c) if (index(c[n], p) == 1) print q[n] }'
  done | sort | paste -sd, - | sed 's/,/, /g'
}

expect source.zbar.version 0.23.93
load_sources SOURCE_BUNDLE_URL
zbar_ver=$(one source.zbar.version)
zbar_url=$(one source.zbar.url)
zbar_sha=$(one source.zbar.sha256)
zbar_files=$(files_of "ZBar ")

tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT

{
cat <<EOF
THIRD-PARTY NOTICES

jzbar is licensed under the GNU Lesser General Public License, version 2.1
only (see LICENSE). Its JAR bundles prebuilt native libraries built from the
third-party sources below. Full license texts are in META-INF/licenses/.

Source code
  The ZBar and libiconv source tarballs, the build scripts and the build
  manifests used to produce the native libraries in this JAR are available at:
    $SOURCE_BUNDLE_URL
  They will remain available there for at least three years after this
  version is released.

ZBar $zbar_ver
  License: LGPL-2.1-or-later (META-INF/licenses/zbar/)
  Contained in: $zbar_files
  Source: $zbar_url
          SHA-256 $zbar_sha
  Parts under other terms: ISAAC (public domain); Xiph.Org portions of
  qrdec.c (BSD-style, META-INF/licenses/xiph-ogg/COPYING); Reed-Solomon
  code derived from Henry Minsky's (LGPL, used with permission).
EOF

if has source.iconv.version; then
  iconv_ver=$(one source.iconv.version)
  iconv_url=$(one source.iconv.url)
  iconv_sha=$(one source.iconv.sha256)
  iconv_files=$(files_of "libiconv ")
  cat <<EOF

libiconv $iconv_ver
  License: LGPL-2.1-or-later (META-INF/licenses/libiconv/)
  Contained in: $iconv_files
  Source: $iconv_url
          SHA-256 $iconv_sha
EOF
fi

if has static.crt.package; then
  expect static.crt.license 'spdx:ZPL-2.1'
  expect static.gcc.license 'spdx:GPL-3.0-or-later WITH GCC-exception-3.1 AND GFDL-1.3-or-later'
  expect static.winpthreads.license 'spdx:MIT AND BSD-3-Clause-Clear'
  expect tool.headers.license 'spdx:ZPL-2.1 AND LGPL-2.1-or-later'
  crt_ver=$(one static.crt.version)
  gcc_ver=$(one static.gcc.version)
  pth_ver=$(one static.winpthreads.version)
  hdr_ver=$(one tool.headers.version)
  cat <<EOF

Windows runtime (parts statically linked into zbar.dll)
  mingw-w64 C runtime $crt_ver: ZPL-2.1, with parts under other terms
    (META-INF/licenses/mingw-w64-crt/)
  libgcc, from GCC $gcc_ver: GPL-3.0-or-later WITH GCC-exception-3.1
    (META-INF/licenses/gcc-runtime/)
  winpthreads $pth_ver: MIT and BSD-3-Clause-Clear
    (META-INF/licenses/winpthreads/)
  Built against mingw-w64-headers $hdr_ver: ZPL-2.1, with some headers
    (imported from Wine) under LGPL-2.1-or-later
    (META-INF/licenses/mingw-w64-crt/)
EOF
fi
} > "$tmp"

mv "$tmp" "$ROOT/THIRD-PARTY-NOTICES"
trap - EXIT
echo "wrote $ROOT/THIRD-PARTY-NOTICES"
