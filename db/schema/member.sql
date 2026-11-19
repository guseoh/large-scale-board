CREATE TABLE member
(
    member_id     BIGINT       NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname      VARCHAR(30)  NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    modified_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;