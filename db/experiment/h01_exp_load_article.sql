-- H-01 적재 방식 비교 실험용 테이블. (Step-0-Design 12절) 게시글과 비슷한 모양이고 보조 인덱스는
-- 없다. 보조 인덱스가 있으면 적재 비용에 인덱스 갱신 비용이 섞인다.
-- 적용: Get-Content db/experiment/h01_exp_load_article.sql | docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" large_scale_board'

-- 방식 A, A', B, C, D가 쓴다. PK는 애플리케이션이 만든 Snowflake ID다.
CREATE TABLE IF NOT EXISTS exp_load_article (
    id         BIGINT       NOT NULL,
    board_id   BIGINT       NOT NULL,
    writer_id  BIGINT       NOT NULL,
    title      VARCHAR(100) NOT NULL,
    content    TEXT         NOT NULL,
    created_at DATETIME(3)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- 방식 B'만 쓴다. 다른 점은 PK를 DB가 만든다는 것(AUTO_INCREMENT) 하나뿐이다.
CREATE TABLE IF NOT EXISTS exp_load_article_identity (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    board_id   BIGINT       NOT NULL,
    writer_id  BIGINT       NOT NULL,
    title      VARCHAR(100) NOT NULL,
    content    TEXT         NOT NULL,
    created_at DATETIME(3)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
