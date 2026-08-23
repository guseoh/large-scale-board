package io.github.guseoh.board.comment.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(@NotBlank @Size(max = 1000) String content) {
}
