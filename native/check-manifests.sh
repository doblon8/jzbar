#!/usr/bin/env bash
# Check that the committed native binaries match their manifests.
# Usage: native/check-manifests.sh      (run from anywhere in the repo)
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

RES=$ROOT/src/main/resources/native
MAN=$NATIVE_DIR/manifest
LIC=$NATIVE_DIR/licenses

errors=0
err() { echo "FAIL: $*" >&2; errors=$((errors + 1)); }

# get KEY FILE: print the value of KEY in a manifest (first match).
get() { awk -F= -v k="$1" '$1 == k { sub(/^[^=]*=/, ""); print; exit }' "$2"; }

# license_dir GROUP: directory under native/licenses for a notice group.
license_dir() {
  case "$1" in
    crt) echo mingw-w64-crt ;;
    gcc) echo gcc-runtime ;;
    winpthreads) echo winpthreads ;;
    *) echo "" ;;
  esac
}

load_sources ZBAR_VERSION ZBAR_SHA256 ICONV_VERSION ICONV_SHA256

manifests=$(ls "$MAN"/*.manifest 2>/dev/null || true)
[ -n "$manifests" ] || { echo "FAIL: no manifests in $MAN" >&2; exit 1; }

listed=$(mktemp)
trap 'rm -f "$listed"' EXIT

for m in $manifests; do
  name=$(basename "$m" .manifest)
  echo "== $name"

  [ "$(get platform "$m")" = "$name" ] || err "$name: platform key is '$(get platform "$m")'"

  [ "$(get source.zbar.version "$m")" = "$ZBAR_VERSION" ] || err "$name: ZBar version differs from sources.env"
  [ "$(get source.zbar.sha256 "$m")" = "$ZBAR_SHA256" ] || err "$name: ZBar sha256 differs from sources.env"

  case "$name" in
    windows-*)
      [ "$(get source.iconv.version "$m")" = "$ICONV_VERSION" ] || err "$name: libiconv version differs from sources.env"
      [ "$(get source.iconv.sha256 "$m")" = "$ICONV_SHA256" ] || err "$name: libiconv sha256 differs from sources.env" ;;
  esac

  n=1
  while path=$(get "file.$n.path" "$m"); [ -n "$path" ]; do
    f=$RES/$path
    if [ ! -f "$f" ]; then
      err "$name: $path is listed but missing"
    else
      want_bytes=$(get "file.$n.bytes" "$m")
      want_sha=$(get "file.$n.sha256" "$m")
      [ "$(wc -c < "$f" | tr -d ' ')" = "$want_bytes" ] || err "$name: $path has a different size"
      [ "$(sha256_of "$f")" = "$want_sha" ] || err "$name: $path has a different SHA-256"
    fi
    echo "$path" >> "$listed"
    n=$((n + 1))
  done
  [ "$n" -gt 1 ] || err "$name: no file entries"

  notices=$(grep -E '^notice\..+\.sha256=' "$m" || true)
  while IFS= read -r line; do
    [ -n "$line" ] || continue
    key=${line%%=*}
    want=${line#*=}
    rest=${key#notice.}
    group=${rest%%.*}
    file=${rest#"$group".}
    file=${file%.sha256}
    dir=$(license_dir "$group")
    if [ -z "$dir" ]; then
      err "$name: no license directory known for notice group '$group'"
    elif [ ! -f "$LIC/$dir/$file" ]; then
      err "$name: native/licenses/$dir/$file is missing"
    elif [ "$(sha256_of "$LIC/$dir/$file")" != "$want" ]; then
      err "$name: native/licenses/$dir/$file differs from the manifest"
    fi
  done <<EOF
$notices
EOF
done

# every native binary must be listed exactly once
dups=$(sort "$listed" | uniq -d)
[ -z "$dups" ] || err "listed in more than one manifest: $dups"

present=$(cd "$RES" && find . -type f \( -name '*.so' -o -name '*.dylib' -o -name '*.dll' \) | sed 's|^\./||' | sort)
unlisted=$(comm -23 <(printf '%s\n' "$present") <(sort -u "$listed"))
[ -z "$unlisted" ] || err "not in any manifest: $unlisted"

# check that all license files are present and match the expected SHA256
echo "== licenses"
[ -f "$LIC/SHA256SUMS" ] || err "native/licenses/SHA256SUMS is missing"
if [ -f "$LIC/SHA256SUMS" ]; then
  (cd "$LIC" && sha256sum -c --quiet SHA256SUMS) || err "native/licenses does not match SHA256SUMS"
  unlisted=$(cd "$LIC" && find . -type f ! -name SHA256SUMS | sed 's|^\./||' | sort \
             | comm -23 - <(sed 's/^[0-9a-f]*  //' "$LIC/SHA256SUMS" | sort))
  [ -z "$unlisted" ] || err "not in SHA256SUMS: $unlisted"
fi
for f in zbar/COPYING zbar/LICENSE.md libiconv/COPYING.LIB xiph-ogg/COPYING \
         mingw-w64-crt/COPYING mingw-w64-crt/COPYING.MinGW-w64-runtime.txt \
         mingw-w64-crt/COPYING.MinGW-w64.txt gcc-runtime/COPYING3 \
         gcc-runtime/COPYING.RUNTIME winpthreads/COPYING; do
  [ -f "$LIC/$f" ] || err "required license file missing: native/licenses/$f"
done

# report the result
if [ "$errors" -gt 0 ]; then
  echo "$errors problem(s) found" >&2
  exit 1
fi
echo "All native files match their manifests."
