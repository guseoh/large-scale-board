package io.github.guseoh.board.member.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank String password,
		@NotBlank @Size(max = 30) String nickname) {

	// 레코드의 기본 toString()은 모든 필드를 출력한다. 로그에 비밀번호가 남지 않도록 가린다.
	@Override
	public String toString() {
		return "SignUpRequest[email=" + email + ", password=****, nickname=" + nickname + "]";
	}
}
