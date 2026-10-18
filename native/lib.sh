# Shared helpers for native/build-*.sh. Source it, do not run it.
# Callers set `set -euo pipefail` before sourcing.

[ "${BASH_SOURCE[0]}" != "$0" ] || { echo "native/lib.sh is meant to be sourced" >&2; exit 1; }

NATIVE_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ROOT=$(dirname "$NATIVE_DIR")

# resolve_out DIR: create DIR if needed and print its absolute path.
resolve_out() {
  mkdir -p "$1"
  (cd "$1" && pwd)
}

# load_sources VAR...: load native/sources.env and fail if any VAR is empty.
# tr strips the CRs that a Windows checkout may add. A temp file is used
# instead of `. <(...)` because sourcing a process substitution is unreliable
# in the bash 3.2 that ships with macOS.
load_sources() {
  local envfile v
  envfile=$(mktemp)
  tr -d '\r' < "$ROOT/native/sources.env" > "$envfile"
  . "$envfile"
  rm -f "$envfile"
  for v in "$@"; do
    [ -n "${!v:-}" ] || { echo "FAIL: $v is not set in native/sources.env" >&2; exit 1; }
  done
}

# sha256_check SHA256 FILE: fail unless FILE has the given SHA-256.
sha256_check() {
  if command -v sha256sum >/dev/null 2>&1; then
    echo "$1  $2" | sha256sum -c -
  else
    echo "$1  $2" | shasum -a 256 -c -
  fi
}

# fetch_verify URL SHA256 FILE: download into the current directory and verify.
# With NATIVE_SOURCES_DIR set (an absolute path), the file is copied from that
# directory instead, for example the sources/ directory of the source bundle.
# The hash is checked either way.
fetch_verify() {
  if [ -n "${NATIVE_SOURCES_DIR:-}" ]; then
    cp "$NATIVE_SOURCES_DIR/$(basename "$1")" "$3"
  else
    curl -fsSLo "$3" "$1"
  fi
  sha256_check "$2" "$3"
}

# Pinned upstream tarballs, downloaded into the current directory.
zbar_url() { echo "https://linuxtv.org/downloads/zbar/zbar-$ZBAR_VERSION.tar.bz2"; }
iconv_url() { echo "https://ftp.gnu.org/pub/gnu/libiconv/libiconv-$ICONV_VERSION.tar.gz"; }

fetch_zbar() { fetch_verify "$(zbar_url)" "$ZBAR_SHA256" zbar.tar.bz2; }
fetch_iconv() { fetch_verify "$(iconv_url)" "$ICONV_SHA256" libiconv.tar.gz; }

# sha256_of FILE: print the SHA-256 of FILE.
sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | awk '{print $1}'
  else
    shasum -a 256 "$1" | awk '{print $1}'
  fi
}

# kv KEY VALUE: append KEY=VALUE to $OUT/toolchain.txt (single-line values only).
kv() {
  case "$2" in
    *$'\n'*) echo "FAIL: multi-line value for $1" >&2; exit 1 ;;
  esac
  printf '%s=%s\n' "$1" "$2" >> "$OUT/toolchain.txt"
}
