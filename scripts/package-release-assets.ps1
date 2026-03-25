param(
    [string]$Version = "0.1.0-alpha"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$distRoot = Join-Path $root "dist"
$outDir = Join-Path $distRoot "v$Version"

if (Test-Path $outDir) {
    Remove-Item -Recurse -Force $outDir
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$targets = @(
    @{ Loader = "forge"; Module = "forge-1.7.10"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.12.2"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.16.5"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.19.4"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.20.1"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.21"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.21.1"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.21.3"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.21.4"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.21.5"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "forge"; Module = "forge-1.21.6"; Jar = "blockduplicatortree-0.1.0-alpha.jar" },
    @{ Loader = "fabric"; Module = "fabric-1.20.1"; Jar = "blockduplicatortree-fabric-1.20.1-0.1.0-alpha.jar" },
    @{ Loader = "fabric"; Module = "fabric-1.21"; Jar = "blockduplicatortree-fabric-1.21-0.1.0-alpha.jar" },
    @{ Loader = "fabric"; Module = "fabric-1.21.1"; Jar = "blockduplicatortree-fabric-1.21.1-0.1.0-alpha.jar" },
    @{ Loader = "fabric"; Module = "fabric-1.21.2"; Jar = "blockduplicatortree-fabric-1.21.2-0.1.0-alpha.jar" },
    @{ Loader = "fabric"; Module = "fabric-1.21.3"; Jar = "blockduplicatortree-fabric-1.21.3-0.1.0-alpha.jar" },
    @{ Loader = "fabric"; Module = "fabric-1.21.4"; Jar = "blockduplicatortree-fabric-1.21.4-0.1.0-alpha.jar" },
    @{ Loader = "fabric"; Module = "fabric-1.21.5"; Jar = "blockduplicatortree-fabric-1.21.5-0.1.0-alpha.jar" },
    @{ Loader = "fabric"; Module = "fabric-1.21.6"; Jar = "blockduplicatortree-fabric-1.21.6-0.1.0-alpha.jar" }
)

$manifestLines = @(
    "# Release Manifest",
    "",
    "| Loader | Minecraft | Asset |",
    "| --- | --- | --- |"
)

foreach ($target in $targets) {
    $moduleDir = Join-Path $root ("versions\" + $target.Module)
    $libsDir = Join-Path $moduleDir "build\libs"
    $sourceJar = Join-Path $libsDir $target.Jar

    if (-not (Test-Path $sourceJar)) {
        throw "Missing build artifact: $sourceJar"
    }

    $minecraftVersion = $target.Module.Replace("forge-", "").Replace("fabric-", "")
    $outName = "blockduplicatortree-$($target.Loader)-$minecraftVersion-$Version.jar"
    Copy-Item $sourceJar (Join-Path $outDir $outName) -Force

    $manifestLines += "| $($target.Loader) | $minecraftVersion | $outName |"
}

$manifestPath = Join-Path $outDir "manifest.md"
$manifestLines | Set-Content $manifestPath

Write-Output "Packaged release assets into $outDir"
