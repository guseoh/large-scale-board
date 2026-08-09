package io.github.guseoh.board.article.web;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public final class ArticleRequest {
    private ArticleRequest() {
    }

    public record Create(
            @NotBlank @Size(max = 200) String title,
            @NotBlank String content
    ) {
    }

    public record Update(
            @NotBlank @Size(max = 200) String title,
            @NotBlank String content
    ) {
    }
}
