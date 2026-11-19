package io.github.guseoh.board.member.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SignUpRequestTest {

	@Test
	@DisplayName("toString()에 비밀번호가 노출되지 않는다")
	void toStringMasksPassword() {
		SignUpRequest request = new SignUpRequest("a@example.com", "super-secret-pw", "길동");

		assertThat(request.toString())
				.contains("a@example.com")
				.doesNotContain("super-secret-pw");
	}
}
