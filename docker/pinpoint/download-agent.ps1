# Pinpoint 에이전트 3.1.0 다운로드 & 압축 해제
# 실행: .\docker\pinpoint\download-agent.ps1
# 결과: docker\pinpoint\pinpoint-agent\ 디렉터리 생성

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$AgentDir  = Join-Path $ScriptDir "pinpoint-agent"
$Version   = "3.1.0"
$Tarball   = "pinpoint-agent-$Version.tar.gz"
$Url       = "https://github.com/pinpoint-apm/pinpoint/releases/download/v$Version/$Tarball"
$TmpPath   = Join-Path $env:TEMP $Tarball

if (Test-Path $AgentDir) {
    Write-Host "[INFO] $AgentDir 이미 존재. 건너뜀."
    exit 0
}

Write-Host "[INFO] 에이전트 다운로드 중: $Url"
Invoke-WebRequest -Uri $Url -OutFile $TmpPath -UseBasicParsing

Write-Host "[INFO] 압축 해제 중: $AgentDir"
New-Item -ItemType Directory -Force -Path $AgentDir | Out-Null
tar -xzf $TmpPath -C $AgentDir --strip-components=1

Write-Host "[INFO] 완료: $AgentDir"
Get-ChildItem $AgentDir | Select-Object Name
