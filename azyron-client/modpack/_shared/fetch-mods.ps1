# AZYRON CLIENT V3 — dependency mod fetcher (Windows PowerShell)
#
# Downloads the Fabric API jar and every performance mod listed in the
# azyron-modpack.txt manifest for a given Minecraft version, straight from
# the official Fabric Maven and the Modrinth API. Run this on a machine with
# normal internet access.
#
# Usage:
#   .\fetch-mods.ps1 -Minecraft 26.3 [-OutputDir .\mods]

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('1.21.11', '26.1', '26.2', '26.3')]
    [string]$Minecraft,

    [string]$OutputDir = (Join-Path (Get-Location) 'mods')
)

$ErrorActionPreference = 'Stop'
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Manifest = Join-Path $ScriptDir "..\$Minecraft\azyron-modpack.txt"

if (-not (Test-Path $Manifest)) {
    throw "No manifest at $Manifest"
}
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$fabricApi = (Select-String -Path $Manifest -Pattern '^fabric_api=' | Select-Object -First 1).Line -replace '^fabric_api=', ''
if ($fabricApi) {
    $url = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$fabricApi/fabric-api-$fabricApi.jar"
    Write-Host ">> fabric-api $fabricApi"
    Invoke-WebRequest -Uri $url -OutFile (Join-Path $OutputDir "fabric-api-$fabricApi.jar")
}

Get-Content $Manifest | Where-Object { $_ -match '^[a-z0-9-]+\|(required|recommended)' } | ForEach-Object {
    $parts = $_ -split '\|'
    $slug = $parts[0]
    Write-Host ">> $slug"
    $loadersParam = [uri]::EscapeDataString('["fabric"]')
    $gameParam = [uri]::EscapeDataString('["' + $Minecraft + '"]')
    $url = "https://api.modrinth.com/v2/project/$slug/version?loaders=$loadersParam&game_versions=$gameParam"
    try {
        $versions = Invoke-RestMethod -Uri $url -Headers @{ 'User-Agent' = 'azyron-client-fetch' }
    } catch {
        Write-Warning "could not query Modrinth for ${slug}: $_"
        return
    }
    if (-not $versions -or $versions.Count -eq 0) {
        Write-Warning "no Fabric build of $slug for Minecraft $Minecraft — skipping (see pack notes)"
        return
    }
    $pick = $versions[0]
    $file = $pick.files | Where-Object { $_.primary } | Select-Object -First 1
    if (-not $file) { $file = $pick.files[0] }
    $dest = Join-Path $OutputDir $file.filename
    Write-Host "   downloading $($pick.version_number) -> $($file.filename)"
    Invoke-WebRequest -Uri $file.url -OutFile $dest
}

Write-Host ""
Write-Host "Done. Contents of ${OutputDir}:"
Get-ChildItem $OutputDir | ForEach-Object { Write-Host "  $($_.Name)" }
Write-Host ""
Write-Host "Next: add the AZYRON CLIENT V3 jar built by CI into $OutputDir,"
Write-Host "then point your launcher profile at it."
