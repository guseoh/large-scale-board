package io.github.guseoh.board.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

	@Test
	@DisplayName("가입하면 이메일은 앞뒤 공백을 지우고 소문자로 정규화한다")
	void registerNormalizesEmail() {
		Member member = Member.register(1L, "  Hong.Gildong@Example.COM ", "hash", "길동");

		assertThat(member.getEmail()).isEqualTo("hong.gildong@example.com");
	}

	@Test
	@DisplayName("가입한 회원은 USER 권한이고 아직 저장되지 않은 새 엔티티다")
	void registerCreatesNewUser() {
		Member member = Member.register(1L, "a@example.com", "hash", " 길동 ");

		assertThat(member.getId()).isEqualTo(1L);
		assertThat(member.getRole()).isEqualTo(Role.USER);
		assertThat(member.getNickname()).isEqualTo("길동");
		assertThat(member.isNew()).isTrue();
	}

	@Test
	@DisplayName("필수 값이 null이면 가입할 수 없다")
	void registerRejectsNull() {
		assertThatNullPointerException()
				.isThrownBy(() -> Member.register(1L, null, "hash", "길동"));
		assertThatNullPointerException()
				.isThrownBy(() -> Member.register(1L, "a@example.com", null, "길동"));
	}
}
