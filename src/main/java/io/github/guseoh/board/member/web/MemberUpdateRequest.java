package io.github.guseoh.board.member.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberUpdateRequest(@NotBlank @Email String email) {
}
