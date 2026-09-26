# One-time setup for Windows: checks for Java 17+, Node.js 18+, and MongoDB,
# installs anything missing with winget, then downloads project dependencies.
# Safe to run again; anything already installed is skipped.
# Run it through setup.cmd in the project folder:
#
#   .\setup.cmd          ask before installing each missing tool
#   .\setup.cmd --yes    install missing tools without asking
#
# Written for Windows PowerShell 5.1 (built into Windows 10/11); ASCII only on purpose.

param([string]$Mode = '')

$ErrorActionPreference = 'Continue'
$AssumeYes = ($Mode -eq '--yes') -or ($Mode -eq '-y') -or ($Mode -eq '-Yes')
$Root = Split-Path -Parent $PSScriptRoot
$Problems = 0

function Ok($msg)   { Write-Host "[OK] $msg" -ForegroundColor Green }
function Info($msg) { Write-Host " * $msg" -ForegroundColor Cyan }
function Warn($msg) { Write-Host "[!] $msg" -ForegroundColor Yellow }
function Fail($msg) { Write-Host "[X] $msg" -ForegroundColor Red }
function Step($msg) { Write-Host ""; Write-Host $msg -ForegroundColor White }

function Ask($question) {
  if ($AssumeYes) { return $true }
  $reply = Read-Host "  $question [Y/n]"
  return ($reply -eq '' -or $reply -match '^[Yy]')
}

# Pick up PATH / JAVA_HOME changes made by installers without reopening the terminal
function Refresh-Env {
  $machine = [Environment]::GetEnvironmentVariable('Path', 'Machine')
  $user = [Environment]::GetEnvironmentVariable('Path', 'User')
  $env:Path = "$machine;$user"
  $jh = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine')
  if (-not $jh) { $jh = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User') }
  if ($jh) { $env:JAVA_HOME = $jh }
}

function Winget-Install($name, $id) {
  if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    Fail "$name is missing and winget isn't available to install it."
    Warn "Install 'App Installer' from the Microsoft Store (it provides winget), or install $name manually, then run setup again."
    $script:Problems++
    return $false
  }
  if (Ask "Install $name with winget?") {
    Info "Running: winget install --id $id -e"
    Info "If Windows asks for permission, click Yes."
    winget install --id $id -e --accept-source-agreements --accept-package-agreements
    if ($LASTEXITCODE -eq 0) {
      Refresh-Env
      Ok "$name installed"
      return $true
    }
    Fail "Installing $name failed (winget exit code $LASTEXITCODE; see the output above)."
  } else {
    Fail "Skipped $name. The app can't run without it."
  }
  $script:Problems++
  return $false
}

# ---------- Java 17+ ----------

function Get-JavaMajor($javaExe) {
  if (-not (Test-Path $javaExe)) { return $null }
  $out = & cmd /c "`"$javaExe`" -version 2>&1"
  $text = ($out | Out-String)
  if ($text -match 'version "(\d+)(?:\.(\d+))?') {
    $major = [int]$Matches[1]
    if ($major -eq 1) { return [int]$Matches[2] }
    return $major
  }
  return $null
}

function Find-JavaHome {
  $candidates = @()
  if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
  foreach ($base in @("$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Java", "$env:ProgramFiles\Microsoft")) {
    if (Test-Path $base) {
      $candidates += (Get-ChildItem $base -Directory | Sort-Object Name -Descending | ForEach-Object { $_.FullName })
    }
  }
  $onPath = Get-Command java.exe -ErrorAction SilentlyContinue
  if ($onPath) { $candidates += (Split-Path -Parent (Split-Path -Parent $onPath.Source)) }
  foreach ($jdkHome in $candidates) {
    $major = Get-JavaMajor (Join-Path $jdkHome 'bin\java.exe')
    if ($major -and $major -ge 17) { return @{ Home = $jdkHome; Major = $major } }
  }
  return $null
}

# ---------- MongoDB ----------

function Find-Mongod {
  $onPath = Get-Command mongod.exe -ErrorAction SilentlyContinue
  if ($onPath) { return $onPath.Source }
  $base = "$env:ProgramFiles\MongoDB\Server"
  if (Test-Path $base) {
    foreach ($dir in (Get-ChildItem $base -Directory | Sort-Object Name -Descending)) {
      $exe = Join-Path $dir.FullName 'bin\mongod.exe'
      if (Test-Path $exe) { return $exe }
    }
  }
  return $null
}

function Test-Port($port) {
  try {
    $client = New-Object System.Net.Sockets.TcpClient
    $result = $client.BeginConnect('127.0.0.1', $port, $null, $null)
    $connected = $result.AsyncWaitHandle.WaitOne(1000) -and $client.Connected
    $client.Close()
    return $connected
  } catch { return $false }
}

# ================= main =================

Write-Host "CinemaFlex setup (Windows)" -ForegroundColor White

Step "1/4  Java 17+"
$java = Find-JavaHome
if ($java) {
  Ok "Java $($java.Major)  ($($java.Home))"
} else {
  Warn "Java 17 or newer was not found."
  if (Winget-Install 'Java 21 (Eclipse Temurin JDK)' 'EclipseAdoptium.Temurin.21.JDK') { $java = Find-JavaHome }
}
if ($java) { $env:JAVA_HOME = $java.Home }

Step "2/4  Node.js 18+"
function Get-NodeMajor {
  if (-not (Get-Command node.exe -ErrorAction SilentlyContinue)) { return 0 }
  $v = (& node --version) -replace '^v', ''
  return [int]($v.Split('.')[0])
}
$nodeMajor = Get-NodeMajor
if ($nodeMajor -ge 18) {
  Ok "Node.js $(& node --version)"
} else {
  if ($nodeMajor -gt 0) { Warn "Node.js $(& node --version) is too old (need 18+)." } else { Warn "Node.js was not found." }
  [void](Winget-Install 'Node.js LTS' 'OpenJS.NodeJS.LTS')
}

Step "3/4  MongoDB"
$mongod = Find-Mongod
if ($mongod) {
  Ok "MongoDB  ($mongod)"
} elseif (Test-Port 27017) {
  Ok "A MongoDB server is already running on port 27017"
} else {
  Warn "MongoDB was not found."
  [void](Winget-Install 'MongoDB Community Server' 'MongoDB.Server')
}

Step "4/4  Project dependencies"
if (Get-Command npm.cmd -ErrorAction SilentlyContinue) {
  Info "Installing frontend packages (npm install)..."
  Push-Location (Join-Path $Root 'frontend')
  & npm.cmd install --no-fund --no-audit
  $npmExit = $LASTEXITCODE
  Pop-Location
  if ($npmExit -eq 0) { Ok "Frontend packages installed" } else { Fail "npm install failed (see the output above)."; $Problems++ }
} else {
  Fail "Skipping frontend packages because npm isn't available yet."
  $Problems++
}

if ($java) {
  Info "Downloading backend dependencies and compiling (first run can take a few minutes)..."
  Push-Location (Join-Path $Root 'cinema-ebooking-backend')
  & cmd /c "mvnw.cmd -q -B -DskipTests compile"
  $mvnExit = $LASTEXITCODE
  Pop-Location
  if ($mvnExit -eq 0) { Ok "Backend compiled" } else { Fail "Backend build failed (see the output above)."; $Problems++ }
} else {
  Fail "Skipping backend build because Java isn't available yet."
  $Problems++
}

Write-Host ""
if ($Problems -eq 0) {
  Write-Host "Setup complete! Start the app with:" -ForegroundColor Green
  Write-Host ""
  Write-Host "    npm start" -ForegroundColor White
  Write-Host ""
  exit 0
} else {
  Write-Host "Setup finished with $Problems problem(s). Fix the items marked [X] above, then run .\setup.cmd again." -ForegroundColor Red
  Write-Host "Tip: if a tool was just installed but still isn't found, close this terminal, open a new one, and rerun setup." -ForegroundColor DarkGray
  exit 1
}
