#!/usr/bin/env bash
# Build the source bundle that accompanies a jzbar release: the pinned ZBar and
# libiconv tarballs plus everything needed to rebuild the native libraries.
#
# The bundle is made from the committed state (git archive of HEAD), not from the
# working tree, and fails if the bundled paths have uncommitted changes. Given the
# same commit and the same tar and gzip versions, it is byte-for-byte reproducible.
#
# Needs git, curl, GNU tar and gzip.
# Usage: native/make-source-bundle.sh VERSION [output-dir]   (default: <repo>/target)
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

fail() { echo "FAIL: $*" >&2; exit 1; }

[ $# -ge 1 ] || fail "usage: native/make-source-bundle.sh VERSION [output-dir]"
VERSION=$1
case "$VERSION" in
  ""|*[!A-Za-z0-9._-]*) fail "bad version: '$VERSION'" ;;
esac
OUT=$(resolve_out "${2:-$ROOT/target}")

load_sources ZBAR_VERSION ZBAR_SHA256 ICONV_VERSION ICONV_SHA256

tar --version | grep -q 'GNU tar' || fail "GNU tar is required"

# What goes into the bundle, besides the upstream tarballs.
FILES=(
  native
  .github/workflows/native-windows-x64.yaml
  .github/workflows/native-linux.yaml
  .github/workflows/native-macos.yaml
  LICENSE
  THIRD-PARTY-NOTICES
  BUILDING.md
)

cd "$ROOT"
git rev-parse --git-dir >/dev/null 2>&1 || fail "not a git repository"
if [ -n "$(git status --porcelain -- "${FILES[@]}")" ]; then
  fail "uncommitted or untracked changes in the bundled paths; commit them first"
fi

commit=$(git rev-parse --verify HEAD)
epoch=$(git show -s --format=%ct "$commit")
name=jzbar-native-sources-$VERSION

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
stage=$work/$name
mkdir -p "$stage/sources"

# Committed files only. git archive fails if a listed path is missing.
git archive --format=tar "$commit" -- "${FILES[@]}" | tar -x -C "$stage"

# Pinned upstream tarballs, verified against native/sources.env
(
  cd "$stage/sources"
  fetch_zbar
  fetch_iconv
  mv zbar.tar.bz2 "zbar-$ZBAR_VERSION.tar.bz2"
  mv libiconv.tar.gz "libiconv-$ICONV_VERSION.tar.gz"
)

{
  echo "bundle.version=$VERSION"
  echo "jzbar.commit=$commit"
} > "$stage/BUNDLE-INFO.txt"

tar --sort=name --mtime="@$epoch" --owner=0 --group=0 --numeric-owner --format=gnu \
    -C "$work" -cf - "$name" | gzip -9n > "$OUT/$name.tar.gz"

echo "wrote $OUT/$name.tar.gz"
echo "$(sha256_of "$OUT/$name.tar.gz")  $name.tar.gz"
