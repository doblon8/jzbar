#!/usr/bin/env bash
# Write native/manifest/<platform>.manifest from the artifact of a native workflow.
#
# Usage: native/write-manifest.sh PLATFORM ARTIFACT_DIR
#   PLATFORM:     windows-x64 | linux-x64 | linux-aarch64 | macos-x64 | macos-aarch64
#   ARTIFACT_DIR: the unpacked workflow artifact (binaries, toolchain.txt, packages.txt)
#
# Needs git and a POSIX userland, not the build toolchains, so it can run on any
# machine. In GitHub Actions the commit and run URL come from the environment.
# Elsewhere set WORKFLOW_COMMIT and WORKFLOW_RUN_URL to the run that produced
# the artifact.
set -euo pipefail

. "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

fail() { echo "FAIL: $*" >&2; exit 1; }

[ $# -eq 2 ] || fail "usage: native/write-manifest.sh PLATFORM ARTIFACT_DIR"
PLATFORM=$1
[ -d "$2" ] || fail "no such directory: $2"
ART=$(cd "$2" && pwd)

# Output name in the artifact | path under the resources native directory | component
iconv_used=0
case "$PLATFORM" in
  windows-x64)
    WORKFLOW=native-windows-x64.yaml
    iconv_used=1
    OUTPUTS='zbar.dll|windows/x64/zbar.dll|ZBar
libiconv-2.dll|windows/x64/libiconv-2.dll|libiconv' ;;
  linux-x64)
    WORKFLOW=native-linux.yaml
    OUTPUTS='libzbar.so|linux/x64/libzbar.so|ZBar' ;;
  linux-aarch64)
    WORKFLOW=native-linux.yaml
    OUTPUTS='libzbar.so|linux/aarch64/libzbar.so|ZBar' ;;
  macos-x64)
    WORKFLOW=native-macos.yaml
    OUTPUTS='libzbar.dylib|osx/x64/libzbar.dylib|ZBar' ;;
  macos-aarch64)
    WORKFLOW=native-macos.yaml
    OUTPUTS='libzbar.dylib|osx/aarch64/libzbar.dylib|ZBar' ;;
  *) fail "unknown platform: $PLATFORM" ;;
esac

if [ "$iconv_used" = 1 ]; then
  load_sources ZBAR_VERSION ZBAR_SHA256 ICONV_VERSION ICONV_SHA256 ICONV_GPG_FPR
else
  load_sources ZBAR_VERSION ZBAR_SHA256
fi

[ -f "$ART/toolchain.txt" ] || fail "$ART/toolchain.txt is missing"
if grep -qvE '^[A-Za-z0-9._+-]+=' "$ART/toolchain.txt"; then
  fail "toolchain.txt has a line that is not KEY=VALUE"
fi

if [ "${GITHUB_ACTIONS:-}" = true ]; then
  commit=$GITHUB_SHA
  run_url="$GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID"
else
  commit=${WORKFLOW_COMMIT:-}
  run_url=${WORKFLOW_RUN_URL:-}
  [ -n "$commit" ] && [ -n "$run_url" ] \
    || fail "set WORKFLOW_COMMIT and WORKFLOW_RUN_URL to the run that built the artifact"
fi
commit=$(git -C "$ROOT" rev-parse --verify "$commit^{commit}") \
  || fail "commit $commit is not in this repository (fetch it first)"

MANIFEST_DIR=$ROOT/native/manifest
mkdir -p "$MANIFEST_DIR"
tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT

{
  echo "manifest.version=1"
  echo "platform=$PLATFORM"
  echo "workflow.file=.github/workflows/$WORKFLOW"
  echo "workflow.commit=$commit"
  echo "workflow.run_url=$run_url"

  echo "source.zbar.version=$ZBAR_VERSION"
  echo "source.zbar.url=$(zbar_url)"
  echo "source.zbar.sha256=$ZBAR_SHA256"
  if [ "$iconv_used" = 1 ]; then
    echo "source.iconv.version=$ICONV_VERSION"
    echo "source.iconv.url=$(iconv_url)"
    echo "source.iconv.sha256=$ICONV_SHA256"
    echo "source.iconv.gpg_fpr=$ICONV_GPG_FPR"
  fi

  case "$PLATFORM" in
    linux-*)
      container=$(git -C "$ROOT" show "$commit:.github/workflows/$WORKFLOW" \
                  | sed -n 's/^ *container: *//p')
      [ -n "$container" ] || fail "no container line in $WORKFLOW at $commit"
      echo "container.image=$container" ;;
  esac

  n=0
  while IFS='|' read -r file rel comp; do
    [ -f "$ART/$file" ] || fail "$ART/$file is missing"
    case "$comp" in
      ZBar) comp="ZBar $ZBAR_VERSION" ;;
      libiconv) comp="libiconv $ICONV_VERSION" ;;
    esac
    n=$((n + 1))
    echo "file.$n.path=$rel"
    echo "file.$n.bytes=$(wc -c < "$ART/$file" | tr -d ' ')"
    echo "file.$n.sha256=$(sha256_of "$ART/$file")"
    echo "file.$n.component=$comp"
  done <<EOF
$OUTPUTS
EOF

  cat "$ART/toolchain.txt"
} | LC_ALL=C sort > "$tmp"

mv "$tmp" "$MANIFEST_DIR/$PLATFORM.manifest"
trap - EXIT
if [ -f "$ART/packages.txt" ]; then
  cp "$ART/packages.txt" "$MANIFEST_DIR/$PLATFORM.packages.txt"
fi
echo "wrote native/manifest/$PLATFORM.manifest"
