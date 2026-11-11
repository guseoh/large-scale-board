#Requires -Version 7
<#
.SYNOPSIS
    MySQL 데이터 볼륨을 압축해 데이터셋 버전으로 보관한다. (D0-17)

.DESCRIPTION
    순서: 행 수 기록 → MySQL 정상 종료 → 볼륨 전체를 tar.gz로 압축 → 매니페스트 작성 → MySQL 기동.

    mysqldump 대신 볼륨을 통째로 압축하는 이유: 덤프는 복원할 때 모든 행을 다시 INSERT하고 인덱스를
    다시 만든다. 수천만 건이면 수십 분~수 시간이다. 볼륨 압축본은 파일을 풀기만 하면 된다. 대가:
    같은 MySQL 버전에서만 복원된다. 그래서 이미지 태그를 매니페스트에 적는다.

    MySQL을 멈추는 이유: 돌고 있는 MySQL의 데이터 파일을 복사하면 버퍼 풀에만 있고 디스크에 아직 안
    쓴 변경이 빠져 복원본이 깨질 수 있다. 정상 종료하면 InnoDB가 변경을 모두 디스크에 내린다.

.EXAMPLE
    ./scripts/snapshot.ps1 -Version ds-v0 -Seed 20261111 -Note 'exp_load_article 100만 건'
#>
param(
    [Parameter(Mandatory)] [ValidatePattern('^ds-v[0-9][0-9a-z-]*$')] [string]$Version,
    [long]$Seed = 20261111,
    [string]$Note = ''
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root

$volume = 'large-scale-board_mysql-data'
$database = 'large_scale_board'
$helperImage = 'alpine:3.24.3'
$snapshotDir = Join-Path $root 'data/snapshots'
New-Item -ItemType Directory -Force $snapshotDir | Out-Null
$archive = Join-Path $snapshotDir "$Version.tar.gz"
$manifest = Join-Path $snapshotDir "$Version.manifest.md"
if (Test-Path $archive) {
    throw "$archive 이(가) 이미 있습니다. 다른 버전 이름을 쓰거나 기존 파일을 지우세요."
}

function Invoke-MySqlRoot([string]$sql) {
    $sql | docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N 2>/dev/null'
}

# 0. 바이너리 로그 정리: MySQL 8.4는 바이너리 로그가 기본으로 켜져 있어 적재한 행이 모두
#    로그에도 남는다. H-01을 몇 번 돌리면 데이터(약 0.8GB)보다 로그(수 GB)가 커진다.
#    데이터셋에는 로그가 필요 없으므로 새 로그 파일로 넘긴 뒤 이전 로그를 지운다.
#    (지금 쓰는 로그 파일은 지울 수 없다)
Invoke-MySqlRoot 'FLUSH BINARY LOGS; PURGE BINARY LOGS BEFORE NOW();' | Out-Null

# 1. 행 수: information_schema의 TABLE_ROWS는 추정값이라 COUNT(*)로 정확히 센다.
$tables = Invoke-MySqlRoot ("SELECT table_name FROM information_schema.tables " +
    "WHERE table_schema = '$database' ORDER BY table_name;")
$counts = [ordered]@{}
foreach ($t in $tables) {
    $counts[$t] = [long](Invoke-MySqlRoot "SELECT COUNT(*) FROM $database.``$t``;")
}
$image = (docker inspect large-scale-board-mysql --format '{{.Config.Image}}').Trim()

# 2~4. 정상 종료 → 압축 → 기동
Write-Host "MySQL 정지..."
$started = Get-Date
docker compose stop mysql | Out-Null
try {
    Write-Host "볼륨 $volume 압축 → $archive"
    docker run --rm -v "${volume}:/var/lib/mysql:ro" -v "${snapshotDir}:/backup" $helperImage `
        tar czf "/backup/$Version.tar.gz" -C /var/lib/mysql .
    if ($LASTEXITCODE -ne 0) { throw '압축 실패' }
} finally {
    Write-Host "MySQL 기동..."
    docker compose up -d --wait mysql | Out-Null
}
$elapsed = (Get-Date) - $started

# 5. 매니페스트
function Get-Sha256([string]$path) { (Get-FileHash $path -Algorithm SHA256).Hash.ToLower() }
$csvFiles = Get-ChildItem (Join-Path $root 'data/csv') -Filter *.csv -ErrorAction SilentlyContinue
$csvRows = $csvFiles | ForEach-Object {
    "| ``$($_.Name)`` | $($_.Length) | ``$(Get-Sha256 $_.FullName)`` |"
}
$ddlRows = Get-ChildItem (Join-Path $root 'db') -Recurse -Filter *.sql | ForEach-Object {
    $relative = [IO.Path]::GetRelativePath($root, $_.FullName) -replace '\\', '/'
    "| ``$relative`` | ``$(Get-Sha256 $_.FullName)`` |"
}
$size = (Get-Item $archive).Length
$lines = @(
    "# 데이터셋 $Version",
    '',
    "- 생성 일시: $(Get-Date -Format o)",
    "- 커밋: ``$((git rev-parse --short HEAD).Trim())``",
    "- MySQL 이미지: ``$image``",
    "- 시드: ``$Seed``",
    "- 압축 파일: ``$Version.tar.gz`` ($([math]::Round($size / 1MB, 1)) MB)",
    "- 보관 소요 시간(정지~기동): $([math]::Round($elapsed.TotalSeconds, 1))초",
    "- 메모: $Note",
    '',
    '## 행 수',
    '',
    '| 테이블 | 행 수 |',
    '| --- | --- |'
)
$lines += $counts.GetEnumerator() | ForEach-Object { "| $($_.Key) | $($_.Value) |" }
$lines += @('', '## CSV', '', '| 파일 | 바이트 | SHA-256 |', '| --- | --- | --- |') + $csvRows
$lines += @('', '## DDL', '', '| 파일 | SHA-256 |', '| --- | --- |') + $ddlRows
$lines | Set-Content -Path $manifest -Encoding utf8

$sizeMb = [math]::Round($size / 1MB, 1)
Write-Host "완료: $archive ($sizeMb MB, $([math]::Round($elapsed.TotalSeconds, 1))초)"
Write-Host "매니페스트: $manifest"
$counts.GetEnumerator() | ForEach-Object { Write-Host ("  {0,-30} {1,12:N0}" -f $_.Key, $_.Value) }
