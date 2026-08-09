package io.github.guseoh.board.member.web;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public final class MemberRequest {
    private MemberRequest() {
    }

    public record Create(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password
    ) {
    }

    public record Update(@NotBlank @Email String email) {
    }
}
