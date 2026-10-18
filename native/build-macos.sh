#!/usr/bin/env bash
# Build libzbar.dylib (macOS, x86_64 or arm64) from the pinned upstream source.
#
# Requires the Xcode command line tools (clang, strip, codesign, install_name_tool,
# otool, lipo) and curl. macOS provides /usr/lib/libiconv.2.dylib, so libiconv is
# not built.
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

# Pinned versions and hashes. tr strips the CRs that a Windows checkout may add.
# A temp file instead of `. <(...)`: sourcing a process substitution is unreliable
# in the bash 3.2 that ships with macOS.
envfile=$(mktemp)
tr -d '\r' < "$ROOT/native/sources.env" > "$envfile"
. "$envfile"
rm -f "$envfile"
: "${ZBAR_VERSION:?}" "${ZBAR_SHA256:?}"

echo "== Toolchain versions"
uname -m
sw_vers -productVersion
xcodebuild -version | sed -n 1p
xcrun --show-sdk-version
clang --version | sed -n 1p

echo "== Fetch and verify ZBar source"
mkdir -p /tmp/src && cd /tmp/src
fetch_zbar
tar xjf zbar.tar.bz2

echo "== Build ZBar (library only, no video)"
# No libiconv build: macOS ships /usr/lib/libiconv.2.dylib. The build is
# small, so make runs serially and a compile error ends up at the log's end.
cd "/tmp/src/zbar-$ZBAR_VERSION"
./configure --enable-shared --disable-static \
            --disable-video --disable-doc --disable-nls \
            --without-jpeg --without-imagemagick --without-graphicsmagick \
            --without-python --without-gtk --without-gir \
            --without-qt --without-qt6 --without-java \
            --without-xshm --without-xv --without-dbus --without-npapi \
            --without-x \
            CFLAGS="-O2"
make -C zbar

echo "== Collect and verify"
export LC_ALL=C
cp -L "/tmp/src/zbar-$ZBAR_VERSION/zbar/.libs/libzbar.dylib" "$OUT/libzbar.dylib"
cd "$OUT"

# libtool records an absolute install path; replace it. Stripping and
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
kv out.minos "$minos"
kv out.arch "$arch"

# show the hash of the built library for verification
shasum -a 256 libzbar.dylib
