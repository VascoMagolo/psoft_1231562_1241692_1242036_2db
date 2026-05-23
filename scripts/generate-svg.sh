#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

find_all_puml_files() {
    find "$REPO_ROOT" -name "*.puml" | sort
}

find_valid_puml_files() {
    find "$REPO_ROOT" -name "*.puml" -path "*/puml/*" | sort
}

get_svg_path() {
    local puml_file="$1"
    local relative="${puml_file#"$REPO_ROOT/"}"
    local svg_relative
    svg_relative=$(echo "$relative" | sed 's|/puml/|/svg/|')
    svg_relative="${svg_relative%.puml}.svg"
    echo "$REPO_ROOT/$svg_relative"
}

warn_invalid_files() {
    local warned=0
    while IFS= read -r f; do
        if [[ "$f" != */puml/* ]]; then
            if [[ $warned -eq 0 ]]; then
                echo "warning: the following .puml file(s) are not inside a /puml/ folder and will be skipped:" >&2
                warned=1
            fi
            echo "  skipped: ${f#"$REPO_ROOT/"}" >&2
        fi
    done < <(find_all_puml_files)
    if [[ $warned -eq 1 ]]; then echo "" >&2; fi
}

generate() {
    if ! command -v plantuml &>/dev/null; then
        echo "error: plantuml not found on PATH" >&2
        exit 1
    fi

    warn_invalid_files

    local count=0 failed=0
    while IFS= read -r puml_file; do
        local svg_file svg_dir relative
        svg_file=$(get_svg_path "$puml_file")
        svg_dir=$(dirname "$svg_file")
        relative="${puml_file#"$REPO_ROOT/"}"

        printf "  processing: %s ... " "$relative"
        mkdir -p "$svg_dir"
        if plantuml -tsvg -o "$svg_dir" "$puml_file" 2>/tmp/plantuml_err; then
            echo "ok"
            count=$((count + 1))
        else
            echo "FAILED"
            sed 's/^/    /' /tmp/plantuml_err >&2
            failed=$((failed + 1))
        fi
    done < <(find_valid_puml_files)

    echo ""
    echo "Done — $count generated, $failed failed."
    [[ $failed -gt 0 ]] && exit 1 || true
}

list() {
    warn_invalid_files

    local count=0
    while IFS= read -r puml_file; do
        local svg_file
        svg_file=$(get_svg_path "$puml_file")
        printf "  %s\n    -> %s\n" \
            "${puml_file#"$REPO_ROOT/"}" \
            "${svg_file#"$REPO_ROOT/"}"
        count=$((count + 1))
    done < <(find_valid_puml_files)

    echo ""
    echo "$count file(s) found."
}

case "${1:-}" in
    "")  generate ;;
    -l)  list ;;
    *)   echo "usage: $(basename "$0") [-l]" >&2; exit 1 ;;
esac
