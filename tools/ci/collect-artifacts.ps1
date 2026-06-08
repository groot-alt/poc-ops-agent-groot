$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$artifactRoot = Join-Path $repositoryRoot "artifacts/ci"
$backendRoot = Join-Path $repositoryRoot "backend"

if (Test-Path -LiteralPath $artifactRoot) {
    Remove-Item -LiteralPath $artifactRoot -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $artifactRoot | Out-Null
Copy-Item -LiteralPath (Join-Path $backendRoot "pom.xml") -Destination (Join-Path $artifactRoot "backend-pom.xml") -Force

$reportFiles = Get-ChildItem -Path $repositoryRoot -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object {
        $_.FullName -notmatch "[\\/]artifacts[\\/]" -and (
            $_.FullName -match "[\\/]target[\\/](surefire-reports|failsafe-reports)[\\/]" -or (
                $_.FullName -match "[\\/]target[\\/]" -and
                $_.Extension -in @(".jar", ".war")
            )
        )
    }

foreach ($file in $reportFiles) {
    $relativePath = $file.FullName.Substring($repositoryRoot.Length).TrimStart("\", "/")
    $destination = Join-Path $artifactRoot $relativePath
    New-Item -ItemType Directory -Force -Path (Split-Path $destination) | Out-Null
    Copy-Item -LiteralPath $file.FullName -Destination $destination -Force
}

$commit = if ($env:GITHUB_SHA) { $env:GITHUB_SHA } else { "local" }
$metadata = @(
    "task=T005",
    "commit=$commit",
    "generatedAt=$([DateTimeOffset]::UtcNow.ToString('O'))"
)
$metadata | Set-Content -LiteralPath (Join-Path $artifactRoot "build-metadata.txt") -Encoding utf8

Write-Host "Build artifacts collected at $artifactRoot"
