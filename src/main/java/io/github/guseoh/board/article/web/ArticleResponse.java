package io.github.guseoh.board.article.web;
import io.github.guseoh.board.article.domain.Article;
import java.time.LocalDateTime;
public record ArticleResponse(Long id, Long memberId, String username, String title, String content, long likeCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ArticleResponse from(Article article, long likeCount) { return new ArticleResponse(article.getId(), article.getMember().getId(), article.getMember().getUsername(), article.getTitle(), article.getContent(), likeCount, article.getCreatedAt(), article.getUpdatedAt()); }
}
