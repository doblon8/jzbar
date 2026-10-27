# Native libraries

jzbar bundles prebuilt ZBar (and, on Windows, libiconv) binaries under `src/main/resources/native/`. Pinned versions and checksums are in `native/sources.env`; license texts are in `native/licenses/`.

## Rebuilding

Each release has `jzbar-native-sources-VERSION.tar.gz`: the pinned upstream tarballs (`sources/`), the build scripts (`native/`) and the CI workflows.

Linux (Ubuntu 20.04, glibc 2.31, GCC 9.4; packages `build-essential ca-certificates curl pkg-config python3-pip ninja-build`; the script installs meson from `native/meson-requirements.txt`):

    tar xzf jzbar-native-sources-VERSION.tar.gz
    cd jzbar-native-sources-VERSION
    NATIVE_SOURCES_DIR=$PWD/sources native/build-linux.sh out

Windows (x64): in an MSYS2 UCRT64 shell with `mingw-w64-ucrt-x86_64-gcc mingw-w64-ucrt-x86_64-pkgconf make curl` installed:

    NATIVE_SOURCES_DIR=$PWD/sources native/build-windows-x64.sh out

macOS (x64 or aarch64; build on a machine of the target architecture; needs the Xcode command line tools and curl):

    NATIVE_SOURCES_DIR=$PWD/sources native/build-macos.sh out

Without `NATIVE_SOURCES_DIR` the scripts download the tarballs; the SHA-256 is checked either way. Toolchain packages are logged (`packages.txt`, `toolchain.txt`) but not pinned, so a rebuild may not be byte-identical.

## Using your own build

Name the library `libzbar.so` (Linux), `libzbar.dylib` (macOS) or `zbar.dll` (Windows, together with `libiconv-2.dll` in the same directory) and put its directory on `java.library.path`:

    java -Djava.library.path=/path/to/dir -jar app.jar

It is loaded instead of the bundled copy.
