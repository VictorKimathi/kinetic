param(
    [string]$TomcatHome = $env:TOMCAT_HOME,
    [string]$AntBat = "C:\Program Files\Apache NetBeans\extide\ant\bin\ant.bat"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

if (-not (Test-Path $AntBat)) {
    throw "Ant not found at '$AntBat'. Install Ant or update the -AntBat argument."
}

if ([string]::IsNullOrWhiteSpace($TomcatHome) -or -not (Test-Path $TomcatHome)) {
    throw "Tomcat home not found. Set TOMCAT_HOME or pass -TomcatHome 'C:\\path\\to\\apache-tomcat'."
}

$startupBat = Join-Path $TomcatHome "bin\startup.bat"
$webappsDir = Join-Path $TomcatHome "webapps"
if (-not (Test-Path $startupBat)) {
    throw "Tomcat startup script not found at '$startupBat'."
}
if (-not (Test-Path $webappsDir)) {
    throw "Tomcat webapps directory not found at '$webappsDir'."
}

# Load .env values into process env for this run.
$dotEnvPath = Join-Path $projectRoot ".env"
if (Test-Path $dotEnvPath) {
    Get-Content $dotEnvPath | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#') -or -not $line.Contains('=')) { return }
        $parts = $line.Split('=', 2)
        [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), 'Process')
    }
}

Write-Host "Building project with Ant..." -ForegroundColor Cyan
& $AntBat "-Dj2ee.server.home=$TomcatHome" clean dist
if ($LASTEXITCODE -ne 0) {
    throw "Ant build failed with exit code $LASTEXITCODE"
}

$warPath = Join-Path $projectRoot "dist\Kinetic.war"
if (-not (Test-Path $warPath)) {
    throw "WAR not generated at '$warPath'."
}

Write-Host "Copying WAR to Tomcat webapps..." -ForegroundColor Cyan
Copy-Item -Path $warPath -Destination (Join-Path $webappsDir "Kinetic.war") -Force

Write-Host "Starting Tomcat..." -ForegroundColor Cyan
& $startupBat

Write-Host "Done. Open: http://localhost:8080/Kinetic/index.html" -ForegroundColor Green
