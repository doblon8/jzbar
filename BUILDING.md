# Native libraries

jzbar bundles prebuilt ZBar (and, on Windows, libiconv) binaries under `src/main/resources/native/`. Pinned versions and checksums are in `native/sources.env`; license texts are in `native/licenses/`.

## Rebuilding

Each release has `jzbar-native-sources-VERSION.tar.gz`: the pinned upstream tarballs (`sources/`), the build scripts (`native/`) and the CI workflows.

Linux (Ubuntu 20.04, glibc 2.31, GCC 9.4; packages `build-essential ca-certificates curl bzip2 pkg-config`):

    tar xzf jzbar-native-sources-VERSION.tar.gz
    cd jzbar-native-sources-VERSION
    NATIVE_SOURCES_DIR=$PWD/sources native/build-linux.sh out

Windows: TODO. macOS: TODO.

Without `NATIVE_SOURCES_DIR` the scripts download the tarballs; the SHA-256 is checked either way. Toolchain packages are logged (`packages.txt`, `toolchain.txt`) but not pinned, so a rebuild may not be byte-identical.

## Using your own build

Name the library `libzbar.so` (Linux), `libzbar.dylib` (macOS) or `zbar.dll` (Windows) and put its directory on `java.library.path`:

    java -Djava.library.path=/path/to/dir -jar app.jar

It is loaded instead of the bundled copy.
