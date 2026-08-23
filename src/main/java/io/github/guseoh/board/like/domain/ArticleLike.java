package io.github.guseoh.board.like.domain;

import io.github.guseoh.board.article.domain.Article;
import io.github.guseoh.board.global.entity.BaseTimeEntity;
import io.github.guseoh.board.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "article_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_article_likes_article_member",
                columnNames = {"article_id", "member_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public ArticleLike(Article article, Member member) {
        this.article = article;
        this.member = member;
    }

}
