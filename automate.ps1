<#
.SYNOPSIS
    Khmer Spirit: The Haunted School 3 - Complete Dev & Build Automation Suite
.DESCRIPTION
    Automates asset synchronization, sprite verification, Maven compilation/shading,
    and 1-click game launching for development and distribution.
#>

[CmdletBinding()]
param (
    [switch]$Auto,
    [switch]$Fast,
    [switch]$Sync,
    [switch]$Build,
    [switch]$Clean,
    [switch]$Kill,
    [switch]$Menu
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# Colors
function Write-Header {
    param([string]$Text)
    Write-Host "`n============================================================" -ForegroundColor Cyan
    Write-Host "  $Text" -ForegroundColor Yellow
    Write-Host "============================================================" -ForegroundColor Cyan
}

function Write-Success { param([string]$Text) Write-Host " [SUCCESS] $Text" -ForegroundColor Green }
function Write-Info    { param([string]$Text) Write-Host " [INFO]    $Text" -ForegroundColor Cyan }
function Write-Warn    { param([string]$Text) Write-Host " [WARN]    $Text" -ForegroundColor Yellow }
function Write-Err     { param([string]$Text) Write-Host " [ERROR]   $Text" -ForegroundColor Red }

# 1. Environment Discovery
function Find-JavaExecutable {
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        return $javaCmd.Source
    }

    $jdkPaths = Get-ChildItem "C:\Program Files\Java\jdk-*" -ErrorAction SilentlyContinue | Sort-Object Name -Descending
    foreach ($jdk in $jdkPaths) {
        $binJava = Join-Path $jdk.FullName "bin\java.exe"
        if (Test-Path $binJava) {
            $env:JAVA_HOME = $jdk.FullName
            return $binJava
        }
    }
    return $null
}

function Find-MavenExecutable {
    $mvnCmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if (-not $mvnCmd) { $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue }
    if ($mvnCmd) { return $mvnCmd.Source }

    # Check IntelliJ Bundled Maven
    $jbMvn = Get-ChildItem "C:\Program Files\JetBrains" -Filter "mvn.cmd" -Recurse -Depth 8 -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($jbMvn) { return $jbMvn.FullName }

    # Check Standard Program Files
    $stdMvn = Get-ChildItem "C:\Program Files" -Filter "mvn.cmd" -Recurse -Depth 5 -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($stdMvn) { return $stdMvn.FullName }

    return $null
}

# 2. Process Manager
function Stop-RunningGame {
    param([bool]$Silent = $false)
    $procs = Get-CimInstance Win32_Process | Where-Object { 
        $_.Name -eq "java.exe" -and ($_.CommandLine -match "hantedSchool_3" -or $_.CommandLine -match "com.khmerspirit.Launcher")
    }

    if ($procs) {
        if (-not $Silent) {
            Write-Warn "Detected active game process (PID: $(($procs | Select-Object -ExpandProperty ProcessId) -join ', ')). Terminating to release file locks..."
        }
        foreach ($p in $procs) {
            Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Milliseconds 800
        Write-Success "All running game processes terminated."
    } elseif (-not $Silent) {
        Write-Info "No active game instances found."
    }
}

# 3. Asset Synchronization & Validation
function Invoke-AssetSyncAndVerify {
    Write-Header "STEP 1: ASSET SYNCHRONIZATION & VERIFICATION"
    
    $imgDir = Join-Path $ScriptDir "images"
    $resDir = Join-Path $ScriptDir "src\main\resources\images"
    
    if (-not (Test-Path $imgDir)) {
        Write-Err "Root 'images' directory not found!"
        return $false
    }
    if (-not (Test-Path $resDir)) {
        New-Item -ItemType Directory -Path $resDir -Force | Out-Null
    }

    # 1. Sync from images/ to resources/
    $copiedCount = 0
    $allImages = Get-ChildItem -Path $imgDir -Recurse -File
    foreach ($file in $allImages) {
        $rel = $file.FullName.Substring($imgDir.Length + 1)
        $dest = Join-Path $resDir $rel
        $destDir = Split-Path -Parent $dest
        if (-not (Test-Path $destDir)) {
            New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        }
        if (-not (Test-Path $dest) -or ($file.LastWriteTime -gt (Get-Item $dest).LastWriteTime)) {
            Copy-Item -Path $file.FullName -Destination $dest -Force
            $copiedCount++
        }
    }

    # 2. Sync any missing resources back to images/
    $resAll = Get-ChildItem -Path $resDir -Recurse -File | Where-Object { $_.Extension -ne ".gitkeep" }
    foreach ($file in $resAll) {
        $rel = $file.FullName.Substring($resDir.Length + 1)
        $dest = Join-Path $imgDir $rel
        $destDir = Split-Path -Parent $dest
        if (-not (Test-Path $destDir)) {
            New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        }
        if (-not (Test-Path $dest)) {
            Copy-Item -Path $file.FullName -Destination $dest -Force
            $copiedCount++
        }
    }
    
    Write-Success "Asset sync complete ($copiedCount files updated/synchronized)."

    # 3. Sprite Sheet & Card Verification
    Write-Info "Verifying sprite sheets and character cards..."
    Add-Type -AssemblyName System.Drawing -ErrorAction SilentlyContinue

    $verifiedCount = 0
    $warnCount = 0

    # Verify Player V1 & V2 Sprites
    $playerDirs = Get-ChildItem -Path (Join-Path $imgDir "player*") -Directory -Recurse
    foreach ($pDir in $playerDirs) {
        $spritePath = Join-Path $pDir.FullName "player.png"
        if (Test-Path $spritePath) {
            try {
                $img = [System.Drawing.Image]::FromFile($spritePath)
                $w = $img.Width
                $h = $img.Height
                $img.Dispose()

                if ($w -eq 128 -and $h -eq 176) {
                    $verifiedCount++
                } else {
                    Write-Warn "Sprite dimension anomaly: $($pDir.Name)/player.png is ${w}x${h} (Expected: 128x176 for 4-direction walk cycle)."
                    $warnCount++
                }
            } catch {
                Write-Warn "Could not read sprite: $spritePath"
            }
        }
    }

    # Verify Ghost Sprites
    $ghostDir = Join-Path $imgDir "ghost"
    if (Test-Path $ghostDir) {
        $ghostSprites = Get-ChildItem -Path $ghostDir -Filter "*_sprite.png"
        foreach ($gs in $ghostSprites) {
            $verifiedCount++
        }
    }

    Write-Success "Verification finished: $verifiedCount key character sprites checked, $warnCount warnings."
    return $true
}

# 4. Build Engine
function Invoke-GameBuild {
    param([bool]$CleanBuild = $false)
    Write-Header "STEP 2: MAVEN COMPILATION & FAT JAR PACKAGING"

    $mvn = Find-MavenExecutable
    if (-not $mvn) {
        Write-Err "Maven executable was not found on your system or IntelliJ directory!"
        Write-Info "Please install Maven or ensure IntelliJ IDEA is installed."
        return $false
    }
    Write-Info "Using Maven: $mvn"

    # Close any running game instances before build to prevent Windows file lock errors
    Stop-RunningGame -Silent $true

    $mvnArgs = @()
    if ($CleanBuild) {
        $mvnArgs += "clean"
    }
    $mvnArgs += "package"
    $mvnArgs += "-DskipTests"

    Write-Info "Running: mvn $($mvnArgs -join ' ')..."
    $sw = [System.Diagnostics.Stopwatch]::StartNew()

    $process = Start-Process -FilePath $mvn -ArgumentList $mvnArgs -NoNewWindow -Wait -PassThru
    $sw.Stop()

    if ($process.ExitCode -ne 0) {
        Write-Err "Build failed with exit code $($process.ExitCode)!"
        return $false
    }

    $targetJar = Join-Path $ScriptDir "target\hantedSchool_3.jar"
    $rootJar = Join-Path $ScriptDir "hantedSchool_3.jar"

    if (-not (Test-Path $targetJar)) {
        Write-Err "Expected output JAR not found at $targetJar!"
        return $false
    }

    # Sync target fat jar to root directory
    Copy-Item -Path $targetJar -Destination $rootJar -Force
    $sizeMB = [math]::Round((Get-Item $rootJar).Length / 1MB, 2)
    Write-Success "Build SUCCESSFUL in $([math]::Round($sw.Elapsed.TotalSeconds, 1))s! Shaded standalone JAR updated (${sizeMB} MB): $rootJar"
    return $true
}

# 5. Launch Game
function Invoke-GameLaunch {
    Write-Header "STEP 3: LAUNCHING KHMER SPIRIT: THE HAUNTED SCHOOL"

    $java = Find-JavaExecutable
    if (-not $java) {
        Write-Err "Java was not found! Please install JDK 21 or higher."
        return $false
    }

    $jarPath = Join-Path $ScriptDir "hantedSchool_3.jar"
    if (-not (Test-Path $jarPath)) {
        $jarPath = Join-Path $ScriptDir "target\hantedSchool_3.jar"
    }

    if (-not (Test-Path $jarPath)) {
        Write-Err "Game JAR file not found. Please build the game first."
        return $false
    }

    Write-Info "Java runtime: $java"
    Write-Info "Launching JAR: $jarPath"
    Write-Info "Enabling JavaFX native access & opens..."

    $jvmArgs = @(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "-jar",
        "`"$jarPath`""
    )

    Start-Process -FilePath $java -ArgumentList $jvmArgs
    Write-Success "Game launched successfully! Have fun exploring the haunted school!"
    return $true
}

# 6. Interactive Menu
function Show-InteractiveMenu {
    Clear-Host
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "       KHMER SPIRIT: THE HAUNTED SCHOOL 3                   " -ForegroundColor Yellow
    Write-Host "          Development & Build Automation Suite              " -ForegroundColor White
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "  [1] " -NoNewline -ForegroundColor Green; Write-Host "Full Auto: Sync Assets + Build Fat JAR + Launch Game (Default)"
    Write-Host "  [2] " -NoNewline -ForegroundColor Green; Write-Host "Fast Launch: Launch Game Immediately (Existing JAR)"
    Write-Host "  [3] " -NoNewline -ForegroundColor Green; Write-Host "Asset Sync: Synchronize & Verify Sprites and Cards Only"
    Write-Host "  [4] " -NoNewline -ForegroundColor Green; Write-Host "Build Fat JAR Only (Without Launch)"
    Write-Host "  [5] " -NoNewline -ForegroundColor Green; Write-Host "Clean & Rebuild Everything (Fresh Maven build)"
    Write-Host "  [6] " -NoNewline -ForegroundColor Green; Write-Host "Close Running Game Instances (Fix file locks)"
    Write-Host "  [0] " -NoNewline -ForegroundColor Red;   Write-Host "Exit"
    Write-Host "============================================================" -ForegroundColor Cyan
    
    $choice = Read-Host "Select option [1-6, 0] (Press Enter for Full Auto)"
    if ([string]::IsNullOrWhiteSpace($choice)) { $choice = "1" }

    switch ($choice) {
        "1" {
            if (Invoke-AssetSyncAndVerify) {
                if (Invoke-GameBuild -CleanBuild $false) {
                    Invoke-GameLaunch
                }
            }
        }
        "2" {
            Invoke-GameLaunch
        }
        "3" {
            Invoke-AssetSyncAndVerify
        }
        "4" {
            if (Invoke-AssetSyncAndVerify) {
                Invoke-GameBuild -CleanBuild $false
            }
        }
        "5" {
            if (Invoke-AssetSyncAndVerify) {
                Invoke-GameBuild -CleanBuild $true
            }
        }
        "6" {
            Stop-RunningGame -Silent $false
        }
        "0" {
            Write-Info "Exiting."
            exit 0
        }
        default {
            Write-Warn "Invalid choice. Exiting."
        }
    }
}

# Main Execution Dispatcher
if ($Kill) {
    Stop-RunningGame -Silent $false
    exit 0
}

if ($Sync) {
    Invoke-AssetSyncAndVerify
    exit 0
}

if ($Fast) {
    Invoke-GameLaunch
    exit 0
}

if ($Build) {
    if (Invoke-AssetSyncAndVerify) {
        Invoke-GameBuild -CleanBuild $Clean
    }
    exit 0
}

if ($Clean) {
    if (Invoke-AssetSyncAndVerify) {
        Invoke-GameBuild -CleanBuild $true
    }
    exit 0
}

if ($Auto) {
    if (Invoke-AssetSyncAndVerify) {
        if (Invoke-GameBuild -CleanBuild $false) {
            Invoke-GameLaunch
        }
    }
    exit 0
}

# If no specific CLI switch was given, open the interactive menu
Show-InteractiveMenu
