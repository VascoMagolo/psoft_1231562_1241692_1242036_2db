#Requires -Version 5.1
[CmdletBinding()]
param(
    [switch]$List
)

$ErrorActionPreference = 'Stop'
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition

function Find-AllPumlFiles {
    Get-ChildItem -Path $ScriptDir -Filter '*.puml' -Recurse | Sort-Object FullName
}

function Find-ValidPumlFiles {
    Get-ChildItem -Path $ScriptDir -Filter '*.puml' -Recurse |
            Where-Object { $_.FullName -replace '\\', '/' -match '/puml/' } |
            Sort-Object FullName
}

function Get-SvgPath($PumlFile) {
    $relative = $PumlFile.FullName.Substring($ScriptDir.Length + 1)
    $svgRelative = $relative -replace '\\puml\\', '\svg\'
    $svgRelative = [System.IO.Path]::ChangeExtension($svgRelative, '.svg')
    Join-Path $ScriptDir $svgRelative
}

function Invoke-WarnInvalidFiles {
    $warned = $false
    foreach ($f in Find-AllPumlFiles) {
        $normalized = $f.FullName -replace '\\', '/'
        if ($normalized -notmatch '/puml/') {
            if (-not $warned) {
                Write-Warning "the following .puml file(s) are not inside a /puml/ folder and will be skipped:"
                $warned = $true
            }
            Write-Host "  skipped: $($f.FullName.Substring($ScriptDir.Length + 1))" -ForegroundColor Yellow
        }
    }
    if ($warned) { Write-Host '' }
}

function Invoke-Generate {
    if (-not (Get-Command plantuml -ErrorAction SilentlyContinue)) {
        Write-Error "plantuml not found on PATH"
        exit 1
    }

    Invoke-WarnInvalidFiles

    $count = 0; $failed = 0
    foreach ($pumlFile in Find-ValidPumlFiles) {
        $svgFile = Get-SvgPath $pumlFile
        $svgDir  = Split-Path -Parent $svgFile
        $relative = $pumlFile.FullName.Substring($ScriptDir.Length + 1)

        Write-Host "  processing: $relative ... " -NoNewline
        if (-not (Test-Path $svgDir)) { New-Item -ItemType Directory -Path $svgDir | Out-Null }

        $result = & plantuml -tsvg -o $svgDir $pumlFile.FullName 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "ok" -ForegroundColor Green
            $count++
        } else {
            Write-Host "FAILED" -ForegroundColor Red
            $result | ForEach-Object { Write-Host "    $_" -ForegroundColor Red }
            $failed++
        }
    }

    Write-Host ""
    Write-Host "Done -- $count generated, $failed failed."
    if ($failed -gt 0) { exit 1 }
}

function Invoke-List {
    Invoke-WarnInvalidFiles

    $count = 0
    foreach ($pumlFile in Find-ValidPumlFiles) {
        $svgFile  = Get-SvgPath $pumlFile
        $relPuml  = $pumlFile.FullName.Substring($ScriptDir.Length + 1)
        $relSvg   = $svgFile.Substring($ScriptDir.Length + 1)
        Write-Host "  $relPuml"
        Write-Host "    -> $relSvg"
        $count++
    }

    Write-Host ""
    Write-Host "$count file(s) found."
}

if ($List) { Invoke-List } else { Invoke-Generate }