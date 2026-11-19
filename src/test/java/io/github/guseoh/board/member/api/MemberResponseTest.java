package io.github.guseoh.board.member.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guseoh.board.TestcontainersConfiguration;
import io.github.guseoh.board.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 응답 JSON에서 ID가 문자열이고, 2^53을 넘는 값이 손상되지 않는지 확인한다(ADR-005).
 * 스프링이 실제로 쓰는 JsonMapper를 주입받아 검증한다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class MemberResponseTest {

	// 2^53(9007199254740992)보다 크다. JavaScript number로 읽으면 끝자리가 바뀐다.
	private static final long SNOWFLAKE_LIKE_ID = 1_234_567_890_123_456_789L;

	private final JsonMapper jsonMapper;

	@Test
	@DisplayName("ID는 JSON 문자열로 내려가고 값이 그대로 보존된다")
	void idIsSerializedAsString() {
		Member member = Member.register(SNOWFLAKE_LIKE_ID, "a@example.com", "hash", "길동");

		JsonNode json = jsonMapper.valueToTree(MemberResponse.from(member));

		assertThat(json.get("id").isString()).isTrue();
		assertThat(json.get("id").asString()).isEqualTo("1234567890123456789");
	}

	@Test
	@DisplayName("비밀번호 해시는 응답에 포함되지 않는다")
	void passwordHashIsNotExposed() {
		Member member = Member.register(1L, "a@example.com", "secret-hash", "길동");

		String json = jsonMapper.writeValueAsString(MemberResponse.from(member));

		assertThat(json).doesNotContain("secret-hash").doesNotContain("passwordHash");
	}
}
