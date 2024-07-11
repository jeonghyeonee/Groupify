param (
    [string]$adbPath,
    [string]$projectDir
)

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "$projectDir\logs\logcat_$timestamp.txt"

New-Item -ItemType Directory -Force -Path "$projectDir\logs"
& $adbPath logcat -d > $logFile
