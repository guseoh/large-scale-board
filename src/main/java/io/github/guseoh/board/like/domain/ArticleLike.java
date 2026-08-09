package io.github.guseoh.board.like.domain;

import io.github.guseoh.board.article.domain.Article;
import io.github.guseoh.board.global.entity.BaseTimeEntity;
import io.github.guseoh.board.member.domain.Member;
import jakarta.persistence.*;

@Entity
@Table(
        name = "article_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_article_likes_article_member",
                columnNames = {"article_id", "member_id"}
        )
)
public class ArticleLike extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    protected ArticleLike() {
    }

    public ArticleLike(Article article, Member member) {
        this.article = article;
        this.member = member;
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
}
