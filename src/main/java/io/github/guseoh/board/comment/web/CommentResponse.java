package io.github.guseoh.board.comment.web;
import io.github.guseoh.board.comment.domain.Comment;
import java.time.LocalDateTime;
public record CommentResponse(
        Long id,
        Long articleId,
        Long memberId,
        String username,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getMember().getId(),
                comment.getMember().getUsername(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
