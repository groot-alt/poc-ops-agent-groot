$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$excludedDirectories = @(".git", "target", "node_modules", "artifacts", ".cache")
$textExtensions = @(
    ".java", ".kt", ".kts", ".xml", ".json", ".yaml", ".yml", ".properties",
    ".md", ".txt", ".ps1", ".sh", ".cmd", ".ts", ".tsx", ".js", ".jsx"
)
$secretPatterns = @(
    "-----BEGIN (RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----",
    "AKIA[0-9A-Z]{16}",
    "ghp_[A-Za-z0-9]{30,}",
    "github_pat_[A-Za-z0-9_]{30,}",
    "sk-[A-Za-z0-9]{32,}"
)

$candidateFiles = Get-ChildItem -Path $repositoryRoot -Recurse -Force -File |
    Where-Object {
        $relativePath = $_.FullName.Substring($repositoryRoot.Length).TrimStart("\", "/")
        $segments = $relativePath -split "[\\/]"
        $isExcluded = $segments | Where-Object { $_ -in $excludedDirectories }
        -not $isExcluded -and
        $_.FullName -ne $PSCommandPath -and
        $_.Extension -in $textExtensions
    }

$findings = foreach ($file in $candidateFiles) {
    foreach ($pattern in $secretPatterns) {
        $matches = Select-String -LiteralPath $file.FullName -Pattern $pattern -AllMatches
        foreach ($match in $matches) {
            [PSCustomObject]@{
                File = $file.FullName.Substring($repositoryRoot.Length).TrimStart("\", "/")
                Line = $match.LineNumber
                Pattern = $pattern
            }
        }
    }
}

if ($findings.Count -gt 0) {
    $findings | Format-Table -AutoSize
    throw "Potential secrets found."
}

Write-Host "Secret scan passed."
