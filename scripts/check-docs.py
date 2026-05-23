#!/usr/bin/env python3
"""Validates the docs/ folder structure.

Expected layout:
  docs/
    README.md
    system-documentation/
      global-artifacts/
        README.md
        furps.md
        glossary.md
        puml/   (at least one .puml)
        svg/    (at least one .svg)
      use-cases/
        WPXA/   (WP + digits + letters, e.g. WP1A, WP2B)
          README.md
          UCD/
            puml/  (at least one .puml)
            svg/   (at least one .svg)
          USxxx/   (US prefix, e.g. US101, US106a)
            README.md
            puml/
              sd_*.puml
              ssd_*.puml
            svg/
              sd_*.svg
              ssd_*.svg

Run manually:  python scripts/check-docs.py
"""

import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
DOCS_ROOT = REPO_ROOT / "docs"
GLOBAL_ARTIFACTS = DOCS_ROOT / "system-documentation" / "global-artifacts"
USE_CASES_ROOT = DOCS_ROOT / "system-documentation" / "use-cases"

WP_PATTERN = re.compile(r"^WP\d+[A-Za-z]+$")
US_PATTERN = re.compile(r"^US", re.IGNORECASE)


def _require_file(directory: Path, name: str, out: list) -> None:
    if not (directory / name).is_file():
        out.append(f"missing file: {name}")


def _require_glob(directory: Path, subdir: str, pattern: str, out: list) -> None:
    if not list((directory / subdir).glob(pattern)):
        out.append(f"missing {subdir}/{pattern}")


def check_global_artifacts(ga: Path) -> list:
    errors = []
    _require_file(ga, "README.md", errors)
    _require_file(ga, "furps.md", errors)
    _require_file(ga, "glossary.md", errors)
    for subdir, pattern in [("puml", "*.puml"), ("svg", "*.svg")]:
        if not (ga / subdir).is_dir():
            errors.append(f"missing directory: {subdir}/")
        else:
            _require_glob(ga, subdir, pattern, errors)
    return errors


def check_ucd(ucd: Path) -> list:
    errors = []
    for subdir, pattern in [("puml", "*.puml"), ("svg", "*.svg")]:
        if not (ucd / subdir).is_dir():
            errors.append(f"missing directory: {subdir}/")
        else:
            _require_glob(ucd, subdir, pattern, errors)
    return errors


def check_us(us: Path) -> list:
    errors = []
    name = us.name.lower()
    _require_file(us, "README.md", errors)
    puml = us / "puml"
    if not puml.is_dir():
        errors.append("missing directory: puml/")
    else:
        for fname in (f"sd_{name}.puml", f"ssd_{name}.puml"):
            if not (puml / fname).is_file():
                errors.append(f"missing file: puml/{fname}")
    svg = us / "svg"
    if not svg.is_dir():
        errors.append("missing directory: svg/")
    else:
        for fname in (f"sd_{name}.svg", f"ssd_{name}.svg"):
            if not (svg / fname).is_file():
                errors.append(f"missing file: svg/{fname}")
    return errors


def check_wp(wp: Path) -> dict:
    results = {}

    wp_errors = []
    _require_file(wp, "README.md", wp_errors)
    if not (wp / "UCD").is_dir():
        wp_errors.append("missing directory: UCD/")
    if wp_errors:
        results[wp.name] = wp_errors

    if (wp / "UCD").is_dir():
        errs = check_ucd(wp / "UCD")
        if errs:
            results[f"{wp.name}/UCD"] = errs

    us_dirs = [d for d in sorted(wp.iterdir()) if d.is_dir() and US_PATTERN.match(d.name)]
    if not us_dirs:
        results.setdefault(wp.name, []).append("no USxxx folders found")
    for us in us_dirs:
        errs = check_us(us)
        if errs:
            results[f"{wp.name}/{us.name}"] = errs

    return results


def _unexpected_dirs(parent: Path, allowed: set) -> list:
    return [
        e.name for e in sorted(parent.iterdir())
        if e.is_dir() and e.name not in allowed
    ]


def main() -> int:
    failures = {}

    if not (DOCS_ROOT / "README.md").is_file():
        failures["docs/"] = ["missing file: README.md"]

    for name in (e.name for e in sorted(DOCS_ROOT.iterdir()) if e.is_dir()):
        if name == "use-cases" or WP_PATTERN.match(name) or US_PATTERN.match(name):
            failures.setdefault("docs/", []).append(f"unexpected directory: {name}/")

    sys_doc = DOCS_ROOT / "system-documentation"
    if not sys_doc.is_dir():
        failures["docs/system-documentation/"] = ["directory does not exist"]
        _report(failures)
        return 1

    for name in _unexpected_dirs(sys_doc, {"global-artifacts", "use-cases"}):
        failures.setdefault("docs/system-documentation/", []).append(
            f"unexpected directory: {name}/ (only global-artifacts/ and use-cases/ expected)"
        )

    if not GLOBAL_ARTIFACTS.is_dir():
        failures["docs/system-documentation/global-artifacts/"] = ["directory does not exist"]
    else:
        errs = check_global_artifacts(GLOBAL_ARTIFACTS)
        if errs:
            failures["docs/system-documentation/global-artifacts/"] = errs

    if not USE_CASES_ROOT.is_dir():
        failures["docs/system-documentation/use-cases/"] = ["directory does not exist"]
    else:
        for name in _unexpected_dirs(USE_CASES_ROOT, set()):
            if not WP_PATTERN.match(name):
                failures.setdefault("docs/system-documentation/use-cases/", []).append(
                    f"unexpected directory: {name}/ (only WP folders expected)"
                )

        for wp in sorted(USE_CASES_ROOT.iterdir()):
            if not wp.is_dir() or not WP_PATTERN.match(wp.name):
                continue
            for rel, errs in check_wp(wp).items():
                failures[f"docs/system-documentation/use-cases/{rel}/"] = errs

    if failures:
        _report(failures)
        return 1

    print("docs structure check passed.")
    return 0


def _report(failures: dict) -> None:
    print("docs structure check FAILED — issues found:")
    for path, errs in failures.items():
        print(f"\n  {path}")
        for e in errs:
            print(f"    - {e}")
    print()


if __name__ == "__main__":
    sys.exit(main())
