package io.github.guseoh.board.comment.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(@NotBlank @Size(max = 1000) String content) {
}
