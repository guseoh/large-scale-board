#Requires -Version 7
<#
.SYNOPSIS
    H-01 적재 방식 비교 실험을 실행한다. (Step-0-Design 12절)

.DESCRIPTION
    방식마다 테이블을 비우고(datagen이 TRUNCATE) 같은 행을 적재해 시간, 실제로 나간 SQL 문장 수,
    MySQL CPU를 잰다. 결과는 data/h01/results.csv에 한 줄씩 쌓이고, 마지막에 방식별 중앙값을
    출력한다.

    SQL 문장 수는 performance_schema에서 board 계정의 INSERT/SELECT/LOAD 실행 횟수를 실행 전후로 뺀
    값이다. datagen 기동 때의 조회 몇 건이 함께 들어간다.

    중단 조건: 한 번의 실행이 MaxSeconds를 넘으면 멈추고 적재한 속도로 전체 시간을
    환산한다(projectedSeconds). 그 방식의 남은 반복은 건너뛴다. 느린 방식(A, A2, B2)은 -Rows
    100000으로 따로 돌려 환산하는 것을 권한다.

.EXAMPLE
    ./scripts/h01.ps1 -Rows 10000 -Repeat 1                          # 모든 방식이 동작하는지 확인
    ./scripts/h01.ps1 -Rows 1000000 -Methods D,C,B                   # 빠른 방식, 100만 건 x 3회
    ./scripts/h01.ps1 -Rows 100000 -Methods A,A2,B2                  # 느린 방식, 10만 건 x 3회
#>
param(
    [long]$Rows = 1000000,
    [ValidateSet('A', 'A2', 'B', 'B2', 'C', 'D')]
    [string[]]$Methods = @('D', 'C', 'B', 'B2', 'A', 'A2'),
    [int]$Repeat = 3,
    [int]$MaxSeconds = 1800,
    [long]$Seed = 20261111
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root

$outDir = Join-Path $root 'data/h01'
$csvDir = (Join-Path $root 'data/csv') -replace '\\', '/'
New-Item -ItemType Directory -Force $outDir, $csvDir | Out-Null
$resultsFile = Join-Path $outDir 'results.csv'
$commit = (git rev-parse --short HEAD).Trim()

function Invoke-MySqlRoot([string]$sql) {
    # 비밀번호는 컨테이너 안의 환경 변수에서 읽는다. 작은따옴표라 PowerShell이 $를 풀지 않는다.
    $sql | docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N 2>/dev/null'
}

function Get-StatementCounts {
    $rows = Invoke-MySqlRoot @"
SELECT EVENT_NAME, COUNT_STAR
FROM performance_schema.events_statements_summary_by_user_by_event_name
WHERE USER = 'board'
  AND EVENT_NAME IN ('statement/sql/insert', 'statement/sql/select', 'statement/sql/load');
"@
    $counts = @{ insert = 0L; select = 0L; load = 0L }
    foreach ($line in $rows) {
        $name, $count = $line -split "`t"
        $counts[$name.Split('/')[-1]] = [long]$count
    }
    return $counts
}

function Start-CpuSampler([string]$file) {
    Start-Job -ArgumentList $file -ScriptBlock {
        param($f)
        while ($true) {
            $t = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
            $cpu = docker stats --no-stream --format '{{.CPUPerc}}' large-scale-board-mysql
            Add-Content -Path $f -Value "$t $($cpu.TrimEnd('%'))"
            Start-Sleep -Seconds 1
        }
    }
}

function Get-CpuStats([string]$file, [long]$fromMs, [long]$toMs) {
    if (-not (Test-Path $file)) { return @{ avg = [double]::NaN; max = [double]::NaN } }
    $values = Get-Content $file | ForEach-Object {
        $t, $v = $_ -split ' '
        if ([long]$t -ge $fromMs -and [long]$t -le $toMs) { [double]$v }
    }
    if (-not $values) { return @{ avg = [double]::NaN; max = [double]::NaN } }
    $m = $values | Measure-Object -Average -Maximum
    return @{ avg = [math]::Round($m.Average, 1); max = [math]::Round($m.Maximum, 1) }
}

if (-not (Test-Path $resultsFile)) {
    @('commit,method,rep,rows,loaded,countInDb,seconds,rowsPerSec',
        'projectedSeconds,stopped,csvSeconds',
        'insertStmts,selectStmts,loadStmts,mysqlCpuAvg,mysqlCpuMax,finishedAt') -join ',' |
        Set-Content $resultsFile
}

Write-Host "H-01 commit=$commit rows=$Rows methods=$($Methods -join ',') repeat=$Repeat seed=$Seed"
$runs = @()
foreach ($method in $Methods) {
    for ($rep = 1; $rep -le $Repeat; $rep++) {
        $tag = "$method-r$rep-$Rows"
        $statsFile = Join-Path $outDir "$tag.cpu.txt"
        Remove-Item $statsFile -ErrorAction SilentlyContinue

        $datagenArgs = @(
            '--datagen.job=h01', "--datagen.method=$method", "--datagen.rows=$Rows",
            "--datagen.seed=$Seed",
            "--datagen.max-seconds=$MaxSeconds", "--datagen.csv-dir=$csvDir"
        )
        # B, B', C는 배치 설정이 켜진 batch 프로필로 실행한다(H01Loader가 맞지 않으면 거부한다).
        if ($method -in 'B', 'B2', 'C') { $datagenArgs += '--spring.profiles.active=batch' }

        $before = Get-StatementCounts
        $sampler = Start-CpuSampler $statsFile
        Write-Host "[$tag] 실행 중..."
        $argLine = $datagenArgs -join ' '
        $output = & .\gradlew.bat :datagen:bootRun --console=plain -q "--args=$argLine" 2>&1
        Stop-Job $sampler; Remove-Job $sampler
        $after = Get-StatementCounts

        $output | Set-Content (Join-Path $outDir "$tag.log")
        $line = ($output | Select-String 'H01_RESULT' | Select-Object -Last 1)
        if (-not $line) {
            Write-Host "[$tag] 실패. 로그: data/h01/$tag.log" -ForegroundColor Red
            break
        }
        $r = @{}
        foreach ($pair in ($line.Line -replace '^.*H01_RESULT ', '' -split ' ')) {
            $k, $v = $pair -split '=', 2
            $r[$k] = $v
        }
        $cpu = Get-CpuStats $statsFile ([long]$r.startEpochMs) ([long]$r.endEpochMs)
        $row = [pscustomobject]@{
            method = $method; rep = $rep; rows = $r.rows; loaded = $r.loaded
            countInDb = $r.countInDb
            seconds = [double]$r.seconds; rowsPerSec = [double]$r.rowsPerSec
            projectedSeconds = [double]$r.projectedSeconds; stopped = $r.stopped
            csvSeconds = $r.csvSeconds
            insertStmts = $after.insert - $before.insert
            selectStmts = $after.select - $before.select
            loadStmts = $after.load - $before.load; mysqlCpuAvg = $cpu.avg; mysqlCpuMax = $cpu.max
        }
        $runs += $row
        ($commit, $row.method, $row.rep, $row.rows, $row.loaded, $row.countInDb, $row.seconds,
            $row.rowsPerSec, $row.projectedSeconds, $row.stopped, $row.csvSeconds, $row.insertStmts,
            $row.selectStmts, $row.loadStmts, $row.mysqlCpuAvg, $row.mysqlCpuMax,
            (Get-Date -Format o)) -join ',' |
            Add-Content $resultsFile
        $stopNote = ''
        if ($row.stopped -eq 'true') { $stopNote = " (중단, 환산 $($row.projectedSeconds)s)" }
        $format = '[{0}] {1:N3}s, {2:N0} rows/s, INSERT {3}, SELECT {4}, LOAD {5}, ' +
            'MySQL CPU avg {6}% max {7}%{8}'
        Write-Host ($format -f $tag, $row.seconds, $row.rowsPerSec, $row.insertStmts,
            $row.selectStmts, $row.loadStmts, $row.mysqlCpuAvg, $row.mysqlCpuMax, $stopNote)

        if ($row.stopped -eq 'true') {
            Write-Host "[$method] 중단 조건에 걸려 남은 반복을 건너뛴다."
            break
        }
    }
}

Write-Host "`n방식별 중앙값 (rows=$Rows)"
$runs | Group-Object method | ForEach-Object {
    $sorted = $_.Group | Sort-Object {
        if ($_.stopped -eq 'true') { $_.projectedSeconds } else { $_.seconds }
    }
    $mid = $sorted[[math]::Floor(($sorted.Count - 1) / 2)]
    [pscustomobject]@{
        method = $_.Name; runs = $_.Count
        seconds = if ($mid.stopped -eq 'true') { "$($mid.projectedSeconds) (환산)" }
                  else { $mid.seconds }
        rowsPerSec = $mid.rowsPerSec; insertStmts = $mid.insertStmts; selectStmts = $mid.selectStmts
        mysqlCpuAvg = $mid.mysqlCpuAvg
    }
} | Format-Table -AutoSize
