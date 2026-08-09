package io.github.guseoh.board.comment.web;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public final class CommentRequest {
    private CommentRequest() {
    }

    public record Create(@NotBlank @Size(max = 1000) String content) {
    }

    public record Update(@NotBlank @Size(max = 1000) String content) {
    }
}
