# Khmer Spirit: The Haunted School - Standalone Windows Packager
$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

$dest = Join-Path $ScriptDir "dist"
$staging = Join-Path $ScriptDir "package_staging"
$zipOutput = Join-Path $ScriptDir "HauntedSchool-v1.0-Windows.zip"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "   PACKAGING HAUNTED SCHOOL FOR STANDALONE WINDOWS RELEASE   " -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Prepare Staging
if (Test-Path $dest) { Remove-Item -Recurse -Force $dest }
if (Test-Path $staging) { Remove-Item -Recurse -Force $staging }
if (Test-Path $zipOutput) { Remove-Item -Force $zipOutput }

New-Item -ItemType Directory -Path $staging | Out-Null
$rootJar = Join-Path $ScriptDir "hantedSchool_3.jar"
if (-not (Test-Path $rootJar)) {
    Write-Error "hantedSchool_3.jar not found! Please build the project first."
}
Copy-Item $rootJar (Join-Path $staging "hantedSchool_3.jar") -Force

# 2. Locate jpackage
$jpackage = "C:\Program Files\Java\jdk-21.0.11\bin\jpackage.exe"
if (-not (Test-Path $jpackage)) {
    $jpackageCmd = Get-Command jpackage.exe -ErrorAction SilentlyContinue
    if ($jpackageCmd) {
        $jpackage = $jpackageCmd.Source
    } else {
        Write-Error "jpackage.exe not found on system!"
    }
}
Write-Host "Using jpackage: $jpackage" -ForegroundColor Cyan

# 3. Execute jpackage to create App Image
Write-Host "Creating standalone application image with bundled Java runtime..." -ForegroundColor Cyan

$jpackageArgs = @(
    "--type", "app-image",
    "--name", "HauntedSchool",
    "--input", $staging,
    "--main-jar", "hantedSchool_3.jar",
    "--main-class", "com.khmerspirit.Launcher",
    "--dest", $dest,
    "--java-options", "--enable-native-access=ALL-UNNAMED",
    "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--vendor", "KhmerSpirit",
    "--app-version", "1.0.0"
)

$sw = [System.Diagnostics.Stopwatch]::StartNew()
$proc = Start-Process -FilePath $jpackage -ArgumentList $jpackageArgs -NoNewWindow -Wait -PassThru
$sw.Stop()

if ($proc.ExitCode -ne 0) {
    Write-Error "jpackage failed with exit code $($proc.ExitCode)"
}

$appDir = Join-Path $dest "HauntedSchool"
$exePath = Join-Path $appDir "HauntedSchool.exe"

if (-not (Test-Path $exePath)) {
    Write-Error "Expected executable not found at $exePath!"
}

Write-Host "Standalone App Image built in $([math]::Round($sw.Elapsed.TotalSeconds, 1))s!" -ForegroundColor Green

# 4. Copy auxiliary folders (saves template, readme)
$readmePath = Join-Path $appDir "HOW_TO_PLAY.txt"
@"
============================================================
       KHMER SPIRIT: THE HAUNTED SCHOOL - VERSION 1.0
============================================================

HOW TO PLAY:
1. Double-click "HauntedSchool.exe" to launch the game.
2. No Java installation is required - the game includes its own optimized runtime.

CONTROLS:
- W, A, S, D or Arrow Keys : Move Character
- E                        : Interact with Doors, Desks, and NPCs
- SPACE                    : Use equipped Active Item / Attack / Flash
- I                        : Open Inventory
- M                        : Open Map / Radar
- ESC                      : Pause Menu / Settings

CREDITS:
Developed by KhmerSpirit Team
============================================================
"@ | Set-Content -Path $readmePath -Encoding UTF8

$savesDir = Join-Path $appDir "saves"
if (-not (Test-Path $savesDir)) {
    New-Item -ItemType Directory -Path $savesDir | Out-Null
}

# 5. Compress to HauntedSchool-v1.0-Windows.zip
Write-Host "Compressing to HauntedSchool-v1.0-Windows.zip..." -ForegroundColor Cyan
$compressSw = [System.Diagnostics.Stopwatch]::StartNew()
Compress-Archive -Path "$appDir\*" -DestinationPath $zipOutput -CompressionLevel Optimal
$compressSw.Stop()

# 6. Cleanup staging
if (Test-Path $staging) { Remove-Item -Recurse -Force $staging }

$zipSizeMB = [math]::Round((Get-Item $zipOutput).Length / 1MB, 2)
Write-Host "============================================================" -ForegroundColor Green
Write-Host " [SUCCESS] HauntedSchool-v1.0-Windows.zip ready!" -ForegroundColor Green
Write-Host " [OUTPUT]  $zipOutput ($zipSizeMB MB)" -ForegroundColor Green
Write-Host " [PLAY]    Extracted app can be launched via HauntedSchool.exe" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
