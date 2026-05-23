#!/usr/bin/env bash
# Installs the pre-commit hook by symlinking scripts/pre-commit into .git/hooks/.
# Run once per machine after cloning: bash scripts/install-hooks.sh

set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
SRC="$REPO_ROOT/scripts/pre-commit"
DST="$REPO_ROOT/.git/hooks/pre-commit"

chmod +x "$SRC"
ln -sf "$SRC" "$DST"
echo "pre-commit hook installed (symlinked to scripts/pre-commit)."
echo "To uninstall: rm .git/hooks/pre-commit"
