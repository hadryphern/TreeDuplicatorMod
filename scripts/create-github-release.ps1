param(
    [Parameter(Mandatory = $true)]
    [string]$Repo,
    [string]$Version = "0.1.0-alpha"
)

$ErrorActionPreference = "Stop"

function Resolve-GhCli {
    $command = Get-Command gh -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $candidates = @(
        "C:\Program Files\GitHub CLI\gh.exe",
        (Join-Path $env:LOCALAPPDATA "Programs\GitHub CLI\gh.exe")
    )

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "GitHub CLI (gh) is not installed or not in PATH."
}

function Assert-GhAuthenticated {
    param(
        [string]$GhCli
    )

    try {
        & $GhCli auth status 1>$null 2>$null
    } catch {
    }

    if ($LASTEXITCODE -ne 0) {
        throw "GitHub CLI is not authenticated. Run: gh auth login"
    }
}

$root = Split-Path -Parent $PSScriptRoot
$packageScript = Join-Path $PSScriptRoot "package-release-assets.ps1"
$releaseTag = "v$Version"
$releaseDir = Join-Path $root ("dist\" + $releaseTag)
$notesPath = Join-Path $root "docs\release-notes\v0.1.0-alpha.md"
$gh = Resolve-GhCli
Assert-GhAuthenticated -GhCli $gh

& $packageScript -Version $Version

$releaseExists = $true
try {
    & $gh release view $releaseTag --repo $Repo 1>$null 2>$null
} catch {
    $releaseExists = $false
}

if (($LASTEXITCODE -ne 0) -or (-not $releaseExists)) {
    & $gh release create $releaseTag --repo $Repo --title $releaseTag --notes-file $notesPath
}

$assets = Get-ChildItem $releaseDir -Filter *.jar | Select-Object -ExpandProperty FullName
foreach ($asset in $assets) {
    & $gh release upload $releaseTag $asset --repo $Repo --clobber
}

Write-Output "GitHub release $releaseTag updated for $Repo"
