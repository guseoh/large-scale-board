package io.github.guseoh.board.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guseoh.board.TestcontainersConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class MemberPersistenceTest {

	private final MemberRepository repository;
	private final JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("delete from member");
	}

	@Test
	@DisplayName("저장한 회원을 이메일로 찾고, 생성 시각이 채워진다")
	void saveAndFindByEmail() {
		repository.save(Member.register(1L, "a@example.com", "hash", "길동"));

		Member found = repository.findByEmail("a@example.com").orElseThrow();

		assertThat(found.getId()).isEqualTo(1L);
		assertThat(found.getRole()).isEqualTo(Role.USER);
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.isNew()).isFalse();
	}

	@Test
	@DisplayName("권한은 ENUM이 아닌 문자열 그대로 저장된다")
	void roleIsStoredAsPlainString() {
		repository.save(Member.register(2L, "b@example.com", "hash", "길동"));

		String raw = jdbcTemplate.queryForObject(
				"select role from member where member_id = 2", String.class);

		assertThat(raw).isEqualTo("USER");
	}

	@Test
	@DisplayName("정규화하면 같아지는 이메일로 가입하면 UNIQUE 제약이 막는다")
	void duplicateEmailIsRejectedByUniqueConstraint() {
		repository.save(Member.register(3L, "c@example.com", "hash", "길동"));

		assertThatThrownBy(
				() -> repository.save(Member.register(4L, "  C@Example.com", "hash", "둘리")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
