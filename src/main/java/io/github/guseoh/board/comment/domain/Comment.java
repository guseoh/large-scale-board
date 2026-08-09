package io.github.guseoh.board.comment.domain;

import io.github.guseoh.board.article.domain.Article;
import io.github.guseoh.board.global.entity.BaseTimeEntity;
import io.github.guseoh.board.member.domain.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 1000)
    private String content;

    protected Comment() {
    }

    public Comment(Article article, Member member, String content) {
        this.article = article;
        this.member = member;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Article getArticle() {
        return article;
    }

    public Member getMember() {
        return member;
    }

    public String getContent() {
        return content;
    }
}
