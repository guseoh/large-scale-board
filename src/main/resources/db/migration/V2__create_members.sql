CREATE TABLE members
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    nickname      VARCHAR(30)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,

    CONSTRAINT pk_members
        PRIMARY KEY (id),

    CONSTRAINT uk_members_email
        UNIQUE (email),

    CONSTRAINT uk_members_nickname
        UNIQUE (nickname),

    CONSTRAINT chk_members_role
        CHECK (role IN ('USER', 'ADMIN'))
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;