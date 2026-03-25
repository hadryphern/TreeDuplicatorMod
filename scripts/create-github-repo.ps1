param(
    [Parameter(Mandatory = $true)]
    [string]$Owner,
    [string]$Repo = "TreeDuplicatorMod",
    [ValidateSet("public", "private")]
    [string]$Visibility = "public",
    [string]$Description = "Monorepo for the Block Duplicator Tree Minecraft mod",
    [bool]$Push = $true
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

$root = Split-Path -Parent $PSScriptRoot
$gh = Resolve-GhCli

try {
    & $gh auth status 1>$null 2>$null
} catch {
}

if ($LASTEXITCODE -ne 0) {
    throw "GitHub CLI is not authenticated. Run: gh auth login"
}

git -C $root rev-parse --is-inside-work-tree 1>$null 2>$null
if (-not $?) {
    throw "The current folder is not a git repository: $root"
}

git -C $root rev-parse --verify HEAD 1>$null 2>$null
if (-not $?) {
    Write-Warning "No commits found yet. Creating remote repository without pushing content."
    $Push = $false
}

if ($Push) {
    git -C $root branch -M main
}

$fullRepo = "$Owner/$Repo"
$repoArgs = @(
    "repo", "create", $fullRepo,
    "--$Visibility",
    "--source", $root,
    "--remote", "origin",
    "--description", $Description
)

if ($Push) {
    $repoArgs += "--push"
}

& $gh @repoArgs

Write-Output "GitHub repository ready: https://github.com/$fullRepo"
