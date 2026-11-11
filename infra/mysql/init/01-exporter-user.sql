-- mysqld-exporter 전용 계정. 볼륨이 비어 있을 때(첫 기동) 한 번만 실행된다.
-- 기존 볼륨에는 직접 한 번 실행한다:
--   Get-Content infra/mysql/init/01-exporter-user.sql | docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"'
--
-- 권한은 지표를 읽는 데 필요한 최소한만 준다.
-- 최대 연결 수 3: exporter가 연결을 새면 앱의 연결 한도를 갉아먹지 않게 한다.
-- 비밀번호는 로컬 실험용 기본값이다(compose.yaml의 MYSQLD_EXPORTER_PASSWORD 기본값과 같아야 한다).
CREATE USER IF NOT EXISTS 'exporter'@'%' IDENTIFIED BY 'exporter-password' WITH MAX_USER_CONNECTIONS 3;
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
