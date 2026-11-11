#Requires -Version 7
<#
.SYNOPSIS
    장애 훈련: 부하를 건 상태에서 MySQL을 멈추고 되살린다. (Step-0-Design 13절)

.DESCRIPTION
    순서: k6 부하 시작(/ping, /ping/db 각 초당 100건) → 정상 구간 → 장애 주입 → 장애 유지
    → 복구 → 회복 관찰.
    주입·복구 시각을 data/drill/<testid>.json에 남긴다. 탐지 시간과 영향은 Prometheus와 Grafana에서
    이 시각을 기준으로 본다(k6 대시보드에서 testid로 고른다).

    주입 방식에 따라 증상이 다르다.
      stop   정상 종료. 새 연결이 바로 거부된다.
      kill   강제 종료(SIGKILL). 진행 중이던 쿼리가 끊기고, 재기동 때 InnoDB가 크래시 복구를 한다.
      pause  프로세스 정지(cgroup freezer). 연결은 살아 있는데 응답이 오지 않는다.
             타임아웃이 없으면 요청이 계속 매달린다.

    앱은 미리 띄워 둔다(./gradlew.bat bootRun). 실험 조건(타임아웃 값)은 -Label로 구분해 기록한다.

.EXAMPLE
    ./scripts/drill-mysql.ps1 -Mode stop -Label default
    ./scripts/drill-mysql.ps1 -Mode pause -Label timeout3s
#>
param(
    [Parameter(Mandatory)] [ValidateSet('stop', 'kill', 'pause')] [string]$Mode,
    [Parameter(Mandatory)] [ValidatePattern('^[a-z0-9-]+$')] [string]$Label,
    [int]$BaselineSeconds = 60,
    [int]$FaultSeconds = 60,
    [int]$RecoverySeconds = 120,
    [int]$Rate = 100
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
$outDir = Join-Path $root 'data/drill'
New-Item -ItemType Directory -Force $outDir | Out-Null

function Now-Ms { [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() }

$stamp = Get-Date -Format 'MMddHHmm'
$testid = "drill-$Mode-$Label-$stamp"
$total = $BaselineSeconds + $FaultSeconds + $RecoverySeconds

# 앱이 떠 있는지 먼저 확인한다. 앱이 없으면 훈련이 아니라 연결 거부만 재게 된다.
try { Invoke-RestMethod -TimeoutSec 3 http://localhost:8081/api/v1/ping | Out-Null }
catch { throw '앱(localhost:8081)이 응답하지 않습니다. 먼저 ./gradlew.bat bootRun 으로 띄우세요.' }

Write-Host "[$testid] k6 부하 시작 (${Rate}/s x 2, ${total}s)"
$k6 = Start-Job -ArgumentList $root, $testid, $total, $Rate -ScriptBlock {
    param($root, $testid, $total, $rate)
    Set-Location $root
    docker compose --profile loadtest run --rm --name "k6-$testid" k6 run --tag "testid=$testid" `
        -e "DURATION=${total}s" -e "RATE=$rate" /scripts/scenarios/drill-mysql.js 2>&1
}

$startedAt = Now-Ms
Start-Sleep -Seconds $BaselineSeconds

Write-Host "[$testid] 장애 주입: docker compose $Mode mysql"
$injectAt = Now-Ms
docker compose $Mode mysql 2>&1 | Out-Null
Start-Sleep -Seconds $FaultSeconds

$recoverCommand = if ($Mode -eq 'pause') { 'unpause' } else { 'start' }
Write-Host "[$testid] 복구: docker compose $recoverCommand mysql"
$recoverAt = Now-Ms
docker compose $recoverCommand mysql 2>&1 | Out-Null
# MySQL이 다시 연결을 받는 시각. kill은 크래시 복구 때문에 stop보다 오래 걸릴 수 있다.
$deadline = (Get-Date).AddSeconds($RecoverySeconds)
$mysqlReadyAt = $null
while ((Get-Date) -lt $deadline -and -not $mysqlReadyAt) {
    docker compose exec -T mysql sh -c 'mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD" --silent 2>/dev/null' | Out-Null
    if ($LASTEXITCODE -eq 0) { $mysqlReadyAt = Now-Ms } else { Start-Sleep -Milliseconds 500 }
}

Write-Host "[$testid] 회복 관찰 중... (k6 종료 대기)"
$k6Output = Receive-Job -Job $k6 -Wait -AutoRemoveJob
$endedAt = Now-Ms
$k6Output | Set-Content (Join-Path $outDir "$testid.k6.txt")

$record = [ordered]@{
    testid = $testid; mode = $Mode; label = $Label; rate = $Rate
    commit = (git rev-parse --short HEAD).Trim()
    startedAt = $startedAt; injectAt = $injectAt; recoverAt = $recoverAt
    mysqlReadyAt = $mysqlReadyAt; endedAt = $endedAt
    mysqlReadySecondsAfterRecover = if ($mysqlReadyAt) {
        [math]::Round(($mysqlReadyAt - $recoverAt) / 1000, 1)
    } else { $null }
}
$record | ConvertTo-Json | Set-Content (Join-Path $outDir "$testid.json")
Write-Host "[$testid] 완료. 기록: data/drill/$testid.json"
$record | Format-List
