# Installs the pre-commit hook by copying scripts/pre-commit into .git/hooks/.
# Run once per machine after cloning: .\scripts\install-hooks.ps1
# Note: uses Copy-Item instead of a symlink to avoid requiring admin privileges.
# If scripts/pre-commit is updated, re-run this script to sync the copy.

$ErrorActionPreference = "Stop"
$repoRoot = (git rev-parse --show-toplevel).Trim()
$src = Join-Path $repoRoot "scripts\pre-commit"
$dst = Join-Path $repoRoot ".git\hooks\pre-commit"

Copy-Item -Force $src $dst
Write-Host "pre-commit hook installed (copied to .git/hooks/pre-commit)."
Write-Host "To uninstall: Remove-Item '$dst'"
