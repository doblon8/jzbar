# Native libraries

jzbar bundles prebuilt ZBar (and, on Windows, libiconv) binaries under `src/main/resources/native/`. Pinned versions and checksums are in `native/sources.env`; license texts are in `native/licenses/`.

## Rebuilding

Each release has `jzbar-native-sources-VERSION.tar.gz`: the pinned upstream tarballs (`sources/`), the build scripts (`native/`) and the CI workflows.

Linux (x64: Ubuntu 20.04, glibc 2.31, GCC 9.4; aarch64: Ubuntu 24.04, glibc 2.39, GCC 13; packages `build-essential ca-certificates curl pkg-config python3-venv ninja-build`; the script installs meson in a temporary virtualenv from `native/meson-requirements.txt`, so it needs network access):

    tar xzf jzbar-native-sources-VERSION.tar.gz
    cd jzbar-native-sources-VERSION
    NATIVE_SOURCES_DIR=$PWD/sources native/build-linux.sh out

Windows (x64): in an MSYS2 UCRT64 shell with `mingw-w64-ucrt-x86_64-gcc mingw-w64-ucrt-x86_64-pkgconf mingw-w64-ucrt-x86_64-meson mingw-w64-ucrt-x86_64-ninja make curl` installed:

    NATIVE_SOURCES_DIR=$PWD/sources native/build-windows-x64.sh out

macOS (x64 or aarch64; build on a machine of the target architecture; needs the Xcode command line tools, curl and `/usr/bin/python3`; the script installs meson and ninja in a temporary virtualenv from `native/meson-requirements.txt` and `native/ninja-requirements.txt`, so it needs network access):

    NATIVE_SOURCES_DIR=$PWD/sources native/build-macos.sh out

Without `NATIVE_SOURCES_DIR` the scripts download the tarballs; the SHA-256 is checked either way. Toolchain packages are logged (`packages.txt`, `toolchain.txt`) but not pinned, so a rebuild may not be byte-identical.

## Using your own build

Name the library `libzbar.so` (Linux), `libzbar.dylib` (macOS) or `zbar.dll` (Windows, together with `libiconv-2.dll` in the same directory) and put its directory on `java.library.path`:

    java -Djava.library.path=/path/to/dir -jar app.jar

It is loaded instead of the bundled copy.
