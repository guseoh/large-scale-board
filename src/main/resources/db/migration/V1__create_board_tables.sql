CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_members_username UNIQUE (username),
    CONSTRAINT uk_members_email UNIQUE (email)
);

CREATE TABLE articles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_articles_member FOREIGN KEY (member_id) REFERENCES members (id)
);

CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles (id),
    CONSTRAINT fk_comments_member FOREIGN KEY (member_id) REFERENCES members (id)
);

CREATE TABLE article_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_article_likes_article_member UNIQUE (article_id, member_id),
    CONSTRAINT fk_article_likes_article FOREIGN KEY (article_id) REFERENCES articles (id),
    CONSTRAINT fk_article_likes_member FOREIGN KEY (member_id) REFERENCES members (id)
);
