package io.github.guseoh.board.article.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content
) {
}
