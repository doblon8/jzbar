# License texts

Files in `zbar/` and `libiconv/` are extracted from the pinned tarballs by
`native/extract-licenses.sh`. The other directories were copied by hand:

| Directory | Origin |
| --- | --- |
| `xiph-ogg/COPYING` | `COPYING` of libogg 1.3.6 (https://downloads.xiph.org/releases/ogg/libogg-1.3.6.tar.gz, SHA-256 `83e6704730683d004d20e21b8f7f55dcb3383cdf84c0daedf30bde175f774638`). ZBar's `qrdec.c` says its bit-reading code is adapted from libogg/libtheora under Xiph.Org's BSD-style license, and does not include the text. |
| `mingw-w64-crt/`, `gcc-runtime/`, `winpthreads/` | License files of the MSYS2 packages `mingw-w64-ucrt-x86_64-crt`, `-gcc` and `-winpthreads` at the versions in `native/manifest/windows-x64.manifest`. |

`SHA256SUMS` lists every file in this directory. `native/check-manifests.sh` verifies it.
