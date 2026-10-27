#!/usr/bin/env bash
# Build libzbar.dylib (macOS, x86_64 or arm64) from the pinned upstream source.
#
# Requires the Xcode command line tools (clang, strip, codesign, install_name_tool,
# otool, lipo, /usr/bin/python3) and curl. macOS provides /usr/lib/libiconv.2.dylib,
# so libiconv is not built. meson and ninja are installed into a temporary virtualenv
# from the hash-pinned native/meson-requirements.txt and native/ninja-requirements.txt
# (ZBar 0.23.94+ needs meson >= 0.61).
#
# Usage: native/build-macos.sh [output-dir]    (default: <repo>/dist)
# The architecture checked in the result is $EXPECT_ARCH (x86_64 or arm64),
# defaulting to the machine's own.
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
OUT=$(resolve_out "${1:-$ROOT/dist}")

export MACOSX_DEPLOYMENT_TARGET=12.0
EXPECT_ARCH=${EXPECT_ARCH:-$(uname -m)}

load_sources ZBAR_VERSION ZBAR_SHA256

echo "== Install meson and ninja (hash-pinned)"
/usr/bin/python3 -m venv --clear /tmp/build-venv
/tmp/build-venv/bin/python -m pip install --disable-pip-version-check \
    --require-hashes --only-binary=:all: --no-deps \
    -r "$ROOT/native/meson-requirements.txt" \
    -r "$ROOT/native/ninja-requirements.txt"
export PATH="/tmp/build-venv/bin:$PATH"

echo "== Toolchain versions"
uname -m
sw_vers -productVersion
xcodebuild -version | sed -n 1p
xcrun --show-sdk-version
clang --version | sed -n 1p
python --version
meson --version
ninja --version

echo "== Fetch and verify ZBar source"
mkdir -p /tmp/src && cd /tmp/src
fetch_zbar
tar xzf zbar.tar.gz

echo "== Build ZBar (library only, no video)"
# No libiconv build: macOS ships /usr/lib/libiconv.2.dylib. /usr is a harmless
# prefix: nothing is installed, the library is copied from the build directory.
cd "/tmp/src/zbar-$ZBAR_VERSION"
meson setup build --prefix=/usr --buildtype=plain -Dc_args=-O2 \
    -Dlibrary_type=shared -Dvideo=false -Ddoc=false -Dnls=disabled \
    -Djpeg=disabled -Dimagemagick=disabled -Dgraphicsmagick=disabled \
    -Dpython=no -Dgtk=no -Dgir=false -Dqt=false -Dqt6=false -Djava=disabled \
    -Dxshm=disabled -Dxv=disabled -Ddbus=disabled -Dnpapi=disabled \
    -Dx11=disabled -Dbuild_tests=false
meson compile -C build

echo "== Collect and verify"
export LC_ALL=C
cp -L "/tmp/src/zbar-$ZBAR_VERSION/build/zbar/libzbar.dylib" "$OUT/libzbar.dylib"
cd "$OUT"

# replace the build install path with @rpath. Stripping and
# editing invalidate the signature, and arm64 refuses to load an
# unsigned dylib, so re-sign ad hoc.
strip -x libzbar.dylib
install_name_tool -id @rpath/libzbar.dylib libzbar.dylib
codesign --force --sign - libzbar.dylib
codesign --verify --strict libzbar.dylib

# single architecture, the expected one
arch=$(lipo -archs libzbar.dylib)
[ "$arch" = "$EXPECT_ARCH" ] || { echo "FAIL: arch is '$arch', expected $EXPECT_ARCH"; exit 1; }

# minimum macOS version
minos=$(otool -l libzbar.dylib | awk '/LC_BUILD_VERSION/{f=1} f&&/minos/{print $2; f=0}')
[ "$minos" = "$MACOSX_DEPLOYMENT_TARGET" ] || { echo "FAIL: minos is '$minos', expected $MACOSX_DEPLOYMENT_TARGET"; exit 1; }

# only its own id plus the system libiconv and libSystem
bad=$(otool -L libzbar.dylib | tail -n +2 | awk '{print $1}' \
      | grep -vE '^(@rpath/libzbar\.dylib|/usr/lib/libiconv\.2\.dylib|/usr/lib/libSystem\.B\.dylib)$' || true)
if [ -n "$bad" ]; then echo "FAIL: unexpected dependencies:"; echo "$bad"; exit 1; fi

# key API must be exported
exports=$(nm -gU libzbar.dylib)
for sym in zbar_version zbar_image_scanner_create; do
  grep -qE " T _$sym\$" <<<"$exports" || { echo "FAIL: missing export $sym"; exit 1; }
done

# no build paths or usernames
if strings -a libzbar.dylib | grep -E '/Users/|/home/|/tmp/|/private/|/Volumes/|/opt/homebrew|/usr/local|runner'; then
  echo "FAIL: build path or username found in binary"; exit 1
fi

echo "arch $arch, minos $minos"

# record the toolchain information
echo "== Record toolchain"
: > "$OUT/toolchain.txt"
kv machine "$(uname -m)"
kv runner.image "${ImageOS:-unknown} ${ImageVersion:-unknown}"
kv tool.macos.version "$(sw_vers -productVersion)"
kv tool.xcode.version "$(xcodebuild -version | sed -n 1p)"
kv tool.xcode.build "$(xcodebuild -version | sed -n 's/^Build version //p')"
kv tool.sdk.version "$(xcrun --show-sdk-version)"
kv tool.clang.version "$(clang --version | sed -n 1p)"
kv tool.python.version "$(python --version | awk '{print $2}')"
kv tool.meson.version "$(meson --version)"
kv tool.ninja.version "$(ninja --version)"
kv out.minos "$minos"
kv out.arch "$arch"

# show the hash of the built library for verification
shasum -a 256 libzbar.dylib
