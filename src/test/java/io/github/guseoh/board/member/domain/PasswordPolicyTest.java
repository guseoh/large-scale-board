package io.github.guseoh.board.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guseoh.board.common.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

	@Test
	@DisplayName("8자 이상이고 72바이트 이하면 통과한다")
	void validPassword() {
		assertThatCode(() -> PasswordPolicy.validate("password1")).doesNotThrowAnyException();
		assertThatCode(() -> PasswordPolicy.validate("a".repeat(72))).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("8자 미만이면 거부한다")
	void tooShort() {
		assertThatThrownBy(() -> PasswordPolicy.validate("short12"))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(MemberErrorCode.MEMBER_INVALID_PASSWORD_POLICY);
	}

	@Test
	@DisplayName("글자 수가 아니라 UTF-8 바이트로 72를 넘으면 거부한다 (한글 25자 = 75바이트)")
	void tooManyBytes() {
		String korean = "가".repeat(25);

		assertThatThrownBy(() -> PasswordPolicy.validate(korean))
				.isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("null은 거부한다")
	void nullPassword() {
		assertThatThrownBy(() -> PasswordPolicy.validate(null))
				.isInstanceOf(BusinessException.class);
	}
}
