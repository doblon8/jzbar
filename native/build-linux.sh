#!/usr/bin/env bash
# Build libzbar.so (Linux, x64 or aarch64) from the pinned upstream source.
#
# Expected environment: Ubuntu 20.04 (glibc 2.31, GCC 9.4), which keeps the
# binary loadable on older distros. The CI container is pinned in
# .github/workflows/native-linux.yaml. Required packages:
#   build-essential ca-certificates curl pkg-config python3-pip ninja-build
# meson comes from native/meson-requirements.txt (hash-pinned): ZBar 0.23.94+
# needs meson >= 0.61 and the distro package is older.
#
# Usage: native/build-linux.sh [output-dir]    (default: <repo>/dist)
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
OUT=$(resolve_out "${1:-$ROOT/dist}")
load_sources ZBAR_VERSION ZBAR_SHA256

echo "== Install meson (hash-pinned)"
python3 -m pip install --disable-pip-version-check --require-hashes \
    --only-binary=:all: --no-deps -r "$ROOT/native/meson-requirements.txt"

echo "== Toolchain versions"
uname -m
gcc --version | sed -n 1p
ld --version | sed -n 1p
ldd --version | sed -n 1p
python3 --version
meson --version
ninja --version
dpkg-query -W gcc-9 binutils libc6 libc6-dev make ninja-build python3

echo "== Fetch and verify ZBar source"
mkdir -p /tmp/src && cd /tmp/src
fetch_zbar
tar xzf zbar.tar.gz

echo "== Build ZBar (library only, no video)"
# No libiconv here: glibc provides iconv itself. /usr is a harmless prefix:
# nothing is installed, the library is copied from the build directory.
cd "/tmp/src/zbar-$ZBAR_VERSION"
meson setup build --prefix=/usr --buildtype=plain \
    -Dc_args=-O2 -Dc_link_args=-static-libgcc \
    -Dlibrary_type=shared -Dvideo=false -Ddoc=false -Dnls=disabled \
    -Djpeg=disabled -Dimagemagick=disabled -Dgraphicsmagick=disabled \
    -Dpython=no -Dgtk=no -Dgir=false -Dqt=false -Dqt6=false -Djava=disabled \
    -Dxshm=disabled -Dxv=disabled -Ddbus=disabled -Dnpapi=disabled \
    -Dx11=disabled -Dbuild_tests=false
meson compile -C build

echo "== Collect and verify"
export LC_ALL=C
cp -L "/tmp/src/zbar-$ZBAR_VERSION/build/zbar/libzbar.so" "$OUT/libzbar.so"
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
record_deb tool.ninja ninja-build
record_deb tool.python3 python3
kv tool.meson.version "$(meson --version)"
kv out.glibc_max "$glibc_max"

# show the hash of the built library for verification
sha256sum libzbar.so
