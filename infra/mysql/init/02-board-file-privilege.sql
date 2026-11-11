-- datagen이 LOAD DATA INFILE(서버가 자기 디스크의 파일을 읽는 방식)을 쓰려면 접속 계정에 FILE
-- 권한이 있어야 한다. FILE은 데이터베이스 단위가 아니라 서버 전체 권한이다. 서버가 읽고 쓸 수 있는
-- 파일은 secure_file_priv (/var/lib/mysql-files) 폴더로 제한되므로, 로컬 실험 환경에서는 앱 계정에
-- 줘도 위험이 작다. 운영이라면 적재 전용 계정을 따로 만든다.
--
-- 볼륨이 비어 있을 때(첫 기동)만 자동 실행된다. 기존 볼륨에는 직접 한 번 실행한다:
--   Get-Content infra/mysql/init/02-board-file-privilege.sql | docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"'
GRANT FILE ON *.* TO 'board'@'%';
