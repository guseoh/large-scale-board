package io.github.guseoh.board.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guseoh.board.TestcontainersConfiguration;
import io.github.guseoh.board.common.error.BusinessException;
import io.github.guseoh.board.member.domain.Member;
import io.github.guseoh.board.member.domain.MemberErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestConstructor;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class MemberSignUpServiceTest {

	private final MemberSignUpService signUpService;
	private final PasswordEncoder passwordEncoder;
	private final JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("delete from member");
	}

	@Test
	@DisplayName("가입하면 비밀번호는 BCrypt 해시로만 저장된다")
	void passwordIsStoredAsBcryptHash() {
		signUpService.signUp("a@example.com", "password123", "길동");

		String stored = jdbcTemplate.queryForObject(
				"select password_hash from member where email = 'a@example.com'", String.class);

		assertThat(stored).startsWith("$2a$10$").hasSize(60).doesNotContain("password123");
		assertThat(passwordEncoder.matches("password123", stored)).isTrue();
	}

	@Test
	@DisplayName("같은 비밀번호도 솔트가 달라서 해시가 매번 다르다")
	void sameRawPasswordProducesDifferentHashes() {
		signUpService.signUp("a@example.com", "password123", "길동");
		signUpService.signUp("b@example.com", "password123", "둘리");

		List<String> hashes = jdbcTemplate.queryForList(
				"select password_hash from member", String.class);

		assertThat(hashes).hasSize(2).doesNotHaveDuplicates();
	}

	@Test
	@DisplayName("이미 가입한 이메일(대소문자·공백 달라도)로 가입하면 MEMBER_EMAIL_DUPLICATED")
	void duplicateEmailIsRejected() {
		signUpService.signUp("a@example.com", "password123", "길동");

		assertThatThrownBy(() -> signUpService.signUp(" A@Example.com ", "password123", "둘리"))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(MemberErrorCode.MEMBER_EMAIL_DUPLICATED);
	}

	@Test
	@DisplayName("정책에 맞지 않는 비밀번호는 저장하지 않는다")
	void invalidPasswordIsNotSaved() {
		assertThatThrownBy(() -> signUpService.signUp("a@example.com", "short", "길동"))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(MemberErrorCode.MEMBER_INVALID_PASSWORD_POLICY);

		assertThat(count()).isZero();
	}

	@Test
	@DisplayName("같은 이메일로 20명이 동시에 가입해도 정확히 1명만 성공하고 나머지는 409다")
	void concurrentSignUpWithSameEmailCreatesExactlyOne() throws Exception {
		int threads = 20;
		CountDownLatch ready = new CountDownLatch(threads);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		List<Future<Object>> futures = new ArrayList<>();
		try {
			for (int i = 0; i < threads; i++) {
				String nickname = "user" + i;
				futures.add(executor.submit(() -> {
					ready.countDown();
					start.await();
					try {
						return signUpService.signUp("same@example.com", "password123", nickname);
					} catch (BusinessException e) {
						return e.getErrorCode();
					}
				}));
			}
			ready.await();
			start.countDown();

			int success = 0;
			int duplicated = 0;
			for (Future<Object> future : futures) {
				Object result = future.get();
				if (result instanceof Member) {
					success++;
				} else if (result == MemberErrorCode.MEMBER_EMAIL_DUPLICATED) {
					duplicated++;
				}
			}

			assertThat(success).isEqualTo(1);
			assertThat(duplicated).isEqualTo(threads - 1);
			assertThat(count()).isEqualTo(1);
		} finally {
			executor.shutdownNow();
		}
	}

	private Integer count() {
		return jdbcTemplate.queryForObject("select count(*) from member", Integer.class);
	}
}
