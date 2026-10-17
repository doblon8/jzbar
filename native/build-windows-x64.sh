#!/usr/bin/env bash
# Build zbar.dll and libiconv-2.dll (Windows x64) from the pinned upstream sources.
#
# Run inside an MSYS2 UCRT64 shell with these packages installed:
#   mingw-w64-ucrt-x86_64-gcc mingw-w64-ucrt-x86_64-pkgconf make curl
#
# Usage: native/build-windows-x64.sh [output-dir]    (default: <repo>/dist)
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"
OUT=$(resolve_out "${1:-$ROOT/dist}")

[ "${MSYSTEM:-}" = UCRT64 ] || { echo "FAIL: run inside an MSYS2 UCRT64 shell" >&2; exit 1; }

TRIPLET=x86_64-w64-mingw32
PREFIX=/usr/local
# strip would otherwise stamp the current time into the PE header
export SOURCE_DATE_EPOCH=0

load_sources ZBAR_VERSION ZBAR_SHA256 ICONV_VERSION ICONV_SHA256

echo "== Toolchain versions"
echo "runner image: ${ImageOS:-unknown} ${ImageVersion:-unknown}"
gcc --version | sed -n 1p
ld --version | sed -n 1p
pacman -Q | grep -E '^(mingw-w64-ucrt-x86_64-|make )'

echo "== Fetch and verify sources"
mkdir -p /tmp/src && cd /tmp/src
fetch_zbar
fetch_iconv
tar xjf zbar.tar.bz2
tar xzf libiconv.tar.gz

echo "== Build libiconv (shared)"
# -std=gnu17: libiconv 1.17 does not compile as C23 (GCC 15 default)
cd "/tmp/src/libiconv-$ICONV_VERSION"
./configure --prefix="$PREFIX" --build=$TRIPLET --host=$TRIPLET \
            --enable-shared --disable-static --disable-nls \
            CFLAGS="-O2 -std=gnu17" \
            LDFLAGS="-Wl,--no-insert-timestamp -Wl,--image-base=0x1c0000000"
make -j"$(nproc)"
make install

echo "== Build ZBar (library only, no video)"
# Fixed link time stamp and image base (an explicit base overrides libtool's
# --enable-auto-image-base) so the same sources give the same DLL bytes.
# clock_gettime comes from winpthreads on MinGW. Offer the linker only the
# static archive so zbar.dll does not import libwinpthread-1.dll.
mkdir -p /tmp/pthread-static
cp /ucrt64/lib/libwinpthread.a /tmp/pthread-static/libwinpthread.a
cp /ucrt64/lib/libwinpthread.a /tmp/pthread-static/libpthread.a
cd "/tmp/src/zbar-$ZBAR_VERSION"
./configure --prefix="$PREFIX" --build=$TRIPLET --host=$TRIPLET \
            --enable-shared --disable-static \
            --disable-video --disable-doc --disable-nls \
            --without-jpeg --without-imagemagick --without-graphicsmagick \
            --without-python --without-gtk --without-gir \
            --without-qt --without-qt6 --without-java \
            --without-xshm --without-xv --without-dbus --without-npapi \
            --with-libiconv-prefix="$PREFIX" \
            CFLAGS="-O2 -std=gnu17" \
            CPPFLAGS="-D__USE_MINGW_ANSI_STDIO=1 -I$PREFIX/include" \
            LDFLAGS="-L/tmp/pthread-static -L$PREFIX/lib -static-libgcc -Wl,--no-insert-timestamp -Wl,--image-base=0x1a0000000"
make -C zbar -j"$(nproc)"

echo "== Collect and verify"
export LC_ALL=C
# zbar.dll is the name jzbar loads. libiconv-2.dll keeps its name
# because zbar.dll imports it by that exact name.
cp "/tmp/src/zbar-$ZBAR_VERSION/zbar/.libs/libzbar-0.dll" "$OUT/zbar.dll"
cp "$PREFIX/bin/libiconv-2.dll" "$OUT/"
strip --strip-unneeded "$OUT"/*.dll
cd "$OUT"

# only system DLLs plus libiconv-2.dll may be imported
bad=$(for f in *.dll; do objdump -p "$f" | awk '/DLL Name/{print $3}'; done \
      | sort -u | grep -viE '^(kernel32|user32|gdi32|api-ms-win-crt-.*|libiconv-2)\.dll$' || true)
if [ -n "$bad" ]; then echo "FAIL: unexpected imports:"; echo "$bad"; exit 1; fi

# key API must be exported
exports=$(objdump -p zbar.dll)
for sym in zbar_version zbar_image_scanner_create; do
  grep -q " $sym" <<<"$exports" || { echo "FAIL: missing export $sym"; exit 1; }
done

# no build paths or usernames
if strings -a -f *.dll | grep -iE '\.pdb|/tmp/|/home/|/d/a/|runner|D:\\|Users'; then
  echo "FAIL: build path or username found in binaries"; exit 1
fi

sha256sum *.dll
