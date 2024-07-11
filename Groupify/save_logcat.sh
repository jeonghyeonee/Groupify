#!/bin/bash

adbPath=$1
projectDir=$2
timestamp=$(date +%Y%m%d_%H%M%S)
logFile="$projectDir/logs/logcat_$timestamp.txt"

mkdir -p "$projectDir/logs"
$adbPath logcat -d > "$logFile"
