#!/usr/bin/env bash
# Build libzbar.so (Linux, x64 or aarch64) from the pinned upstream source.
#
# Expected environment: Ubuntu 20.04 (glibc 2.31, GCC 9.4), which keeps the
# binary loadable on older distros. The CI container is pinned in
# .github/workflows/native-linux.yaml. Required packages:
#   build-essential ca-certificates curl bzip2 pkg-config
#
# Usage: native/build-linux.sh [output-dir]    (default: <repo>/dist)
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
OUT=$(resolve_out "${1:-$ROOT/dist}")
load_sources ZBAR_VERSION ZBAR_SHA256

echo "== Toolchain versions"
uname -m
gcc --version | sed -n 1p
ld --version | sed -n 1p
ldd --version | sed -n 1p
dpkg-query -W gcc-9 binutils libc6 libc6-dev make

echo "== Fetch and verify ZBar source"
mkdir -p /tmp/src && cd /tmp/src
fetch_zbar
tar xjf zbar.tar.bz2

echo "== Build ZBar (library only, no video)"
# No libiconv here: glibc provides iconv itself.
cd "/tmp/src/zbar-$ZBAR_VERSION"
./configure --enable-shared --disable-static \
            --disable-video --disable-doc --disable-nls \
            --without-jpeg --without-imagemagick --without-graphicsmagick \
            --without-python --without-gtk --without-gir \
            --without-qt --without-qt6 --without-java \
            --without-xshm --without-xv --without-dbus --without-npapi \
            --without-x \
            CFLAGS="-O2" LDFLAGS="-static-libgcc"
make -C zbar -j"$(nproc)"

echo "== Collect and verify"
export LC_ALL=C
cp -L "/tmp/src/zbar-$ZBAR_VERSION/zbar/.libs/libzbar.so" "$OUT/libzbar.so"
strip --strip-unneeded "$OUT/libzbar.so"
cd "$OUT"

# only glibc components may be needed (libc, libm, libpthread, libdl, librt, loader)
bad=$(readelf -d libzbar.so | grep NEEDED | sed 's/.*\[\(.*\)\]/\1/' \
      | grep -vE '^(libc\.so\.6|libm\.so\.6|libpthread\.so\.0|libdl\.so\.2|librt\.so\.1|ld-linux-(x86-64|aarch64)\.so\.[12])$' || true)
if [ -n "$bad" ]; then echo "FAIL: unexpected dependencies:"; echo "$bad"; exit 1; fi

# an RPATH/RUNPATH would embed a build path
if readelf -d libzbar.so | grep -E 'RPATH|RUNPATH'; then
  echo "FAIL: RPATH/RUNPATH present"; exit 1
fi

# key API must be exported
exports=$(nm -D --defined-only libzbar.so)
for sym in zbar_version zbar_image_scanner_create; do
  grep -qE " T $sym\$" <<<"$exports" || { echo "FAIL: missing export $sym"; exit 1; }
done

# no build paths or usernames
if strings -a libzbar.so | grep -E '/home/|/tmp/|/root/|/__w/|/github/|runner'; then
  echo "FAIL: build path or username found in binary"; exit 1
fi

glibc_max=$(objdump -T libzbar.so | grep -o 'GLIBC_[0-9.]*' | sort -Vu | tail -1)
echo "Highest glibc symbol version required: $glibc_max"

# record the toolchain information
echo "== Record toolchain"
: > "$OUT/toolchain.txt"
dpkg-query -W | sort > "$OUT/packages.txt"

record_deb() { # PREFIX PACKAGE
  kv "$1.package" "$2"
  kv "$1.version" "$(dpkg-query -W -f='${Version}' "$2")"
}

kv machine "$(uname -m)"
[ -z "${ImageVersion:-}" ] || kv runner.image "${ImageOS:-unknown} $ImageVersion"
kv os.name "$(. /etc/os-release && echo "$PRETTY_NAME")"
record_deb tool.gcc gcc-9
record_deb tool.binutils binutils
record_deb tool.libc6 libc6
record_deb tool.libc6-dev libc6-dev
record_deb tool.make make
kv out.glibc_max "$glibc_max"

# show the hash of the built library for verification
sha256sum libzbar.so
