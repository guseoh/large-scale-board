package io.github.guseoh.board.article.domain;

import io.github.guseoh.board.global.entity.BaseTimeEntity;
import io.github.guseoh.board.member.domain.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "articles")
public class Article extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private Member member;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    protected Article() { }
    public Article(Member member, String title, String content) { this.member = member; this.title = title; this.content = content; }
    public void update(String title, String content) { this.title = title; this.content = content; }
    public Long getId() { return id; } public Member getMember() { return member; } public String getTitle() { return title; } public String getContent() { return content; }
}
