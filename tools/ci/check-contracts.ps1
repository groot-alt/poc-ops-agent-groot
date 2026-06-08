$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
Set-Location $repositoryRoot

$schemaFiles = Get-ChildItem -Path "backend/contracts" -Recurse -File -Filter "*.schema.json"
if ($schemaFiles.Count -lt 6) {
    throw "Expected at least six versioned JSON schemas, found $($schemaFiles.Count)."
}

foreach ($schemaFile in $schemaFiles) {
    $schema = Get-Content -Raw -Encoding UTF8 $schemaFile.FullName | ConvertFrom-Json
    if (-not $schema.'$schema' -or -not $schema.'$id' -or -not $schema.title) {
        throw "Schema metadata is incomplete: $($schemaFile.FullName)"
    }
}

$commandSchema = Get-Content -Raw -Encoding UTF8 "backend/contracts/workflow/read-only-command-v1.schema.json" | ConvertFrom-Json
$commandRequired = @($commandSchema.required)
foreach ($requiredField in @("workflowId", "idempotencyKey", "operationClass", "operator", "policyDecision", "trace")) {
    if ($requiredField -notin $commandRequired) {
        throw "Read-only command schema does not require $requiredField."
    }
}

if ($commandSchema.properties.operationClass.const -ne "READ_ONLY") {
    throw "P1 command contract must only allow READ_ONLY operations."
}

$eventSchema = Get-Content -Raw -Encoding UTF8 "backend/contracts/events/semantic-event-v1.schema.json" | ConvertFrom-Json
$eventRequired = @($eventSchema.required)
foreach ($requiredField in @("eventId", "workflowId", "sequence", "timestamp", "type", "payload")) {
    if ($requiredField -notin $eventRequired) {
        throw "Semantic event schema does not require $requiredField."
    }
}

Get-Content -Raw -Encoding UTF8 "backend/contracts/workflow/examples/read-only-node-health-command.json" | ConvertFrom-Json | Out-Null
Get-Content -Raw -Encoding UTF8 "backend/contracts/events/examples/workflow-completed-event.json" | ConvertFrom-Json | Out-Null

Write-Host "Contract baseline check passed."
