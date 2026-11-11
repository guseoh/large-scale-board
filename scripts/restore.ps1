#Requires -Version 7
<#
.SYNOPSIS
    보관한 데이터셋으로 MySQL 볼륨을 되돌리고, 행 수가 매니페스트와 같은지 확인한다. (D0-17)

.DESCRIPTION
    순서: 전체 중지 → MySQL 컨테이너와 볼륨 삭제 → 빈 볼륨 생성 → 압축 해제 → MySQL 기동 → 행 수
    확인 → 나머지 기동.

    실험은 항상 "복원 → 워밍업 → 측정" 순서로 한다. 복원하면 버퍼 풀이 비어 있는 상태(cold)에서
    시작한다 (board.cnf에서 버퍼 풀 dump/load를 껐다). 그래서 측정 전에 워밍업이 필요하다.

    주의: 현재 MySQL 데이터가 모두 지워진다.

.EXAMPLE
    ./scripts/restore.ps1 -Version ds-v0
#>
param(
    [Parameter(Mandatory)] [string]$Version
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root

$volume = 'large-scale-board_mysql-data'
$database = 'large_scale_board'
$helperImage = 'alpine:3.24.3'
$snapshotDir = Join-Path $root 'data/snapshots'
$archive = Join-Path $snapshotDir "$Version.tar.gz"
$manifest = Join-Path $snapshotDir "$Version.manifest.md"
foreach ($f in $archive, $manifest) {
    if (-not (Test-Path $f)) { throw "$f 이(가) 없습니다." }
}

# 매니페스트의 이미지와 지금 Compose의 이미지가 다르면 복원본을 읽지 못할 수 있다.
$imageMatch = Select-String -Path $manifest -Pattern 'MySQL 이미지: `([^`]+)`'
$expectedImage = $imageMatch.Matches[0].Groups[1].Value
$composeImage = (docker compose config --images | Where-Object { $_ -like 'mysql:*' } | Select-Object -First 1)
if ($expectedImage -ne $composeImage) {
    throw "매니페스트 이미지($expectedImage)와 compose.yaml 이미지($composeImage)가 다릅니다."
}

$started = Get-Date
Write-Host '전체 중지, MySQL 컨테이너와 볼륨 삭제...'
docker compose stop | Out-Null
docker compose rm -f mysql | Out-Null
docker volume rm $volume | Out-Null

# Compose가 볼륨을 만들게 한다(Compose 라벨이 붙어야 이후 경고가 없다). 컨테이너는 만들기만 하고
# 띄우지 않는다.
docker compose create mysql | Out-Null

Write-Host "압축 해제: $archive → $volume"
docker run --rm -v "${volume}:/var/lib/mysql" -v "${snapshotDir}:/backup:ro" $helperImage `
    tar xzf "/backup/$Version.tar.gz" -C /var/lib/mysql
if ($LASTEXITCODE -ne 0) { throw '압축 해제 실패' }

Write-Host 'MySQL 기동...'
docker compose up -d --wait mysql | Out-Null
$elapsed = (Get-Date) - $started

# 행 수 확인
$expected = [ordered]@{}
$inCounts = $false
foreach ($line in Get-Content $manifest) {
    if ($line -eq '## 행 수') { $inCounts = $true; continue }
    if ($inCounts -and $line -like '## *') { break }
    if ($inCounts -and $line -match '^\| ([a-z0-9_]+) \| ([0-9]+) \|$') {
        $expected[$Matches[1]] = [long]$Matches[2]
    }
}
$ok = $true
foreach ($entry in $expected.GetEnumerator()) {
    $sql = "SELECT COUNT(*) FROM $database.``$($entry.Key)``;"
    $actual = [long]($sql | docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N 2>/dev/null')
    $mark = if ($actual -eq $entry.Value) { '일치' } else { $ok = $false; '불일치' }
    $format = '  {0,-30} 기대 {1,12:N0}  실제 {2,12:N0}  {3}'
    Write-Host ($format -f $entry.Key, $entry.Value, $actual, $mark)
}

Write-Host '나머지 서비스 기동...'
docker compose up -d | Out-Null
Write-Host "복원 소요 시간(중지~MySQL 기동): $([math]::Round($elapsed.TotalSeconds, 1))초"
if (-not $ok) { throw '행 수가 매니페스트와 다릅니다.' }
Write-Host "완료: $Version 복원, 행 수 일치"
