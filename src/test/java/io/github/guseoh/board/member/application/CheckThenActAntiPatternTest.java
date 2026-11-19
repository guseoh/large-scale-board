package io.github.guseoh.board.member.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guseoh.board.TestcontainersConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

/**
 * 안티패턴 재현: "있는지 먼저 조회하고, 없으면 넣는다"(check-then-act)는 동시 요청에서 깨진다.
 *
 * <p>두 요청이 모두 "없음"을 본 뒤에 각자 INSERT하도록 CyclicBarrier로 순서를 고정해 매번 같은
 * 결과가 나오게 했다. 운이 나쁠 때만 터지는 버그를 테스트로 확정적으로 재현하려는 장치다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class CheckThenActAntiPatternTest {

	private static final String TABLE = "check_then_act_member";

	private final JdbcTemplate jdbcTemplate;

	@AfterEach
	void tearDown() {
		jdbcTemplate.execute("drop table if exists " + TABLE);
	}

	@Test
	@DisplayName("함정: UNIQUE가 없으면 선조회를 통과한 두 요청이 모두 INSERT해서 중복 행이 생긴다")
	void withoutUniqueConstraintDuplicatesAreCreated() throws Exception {
		createTable(false);

		List<Object> results = signUpTwiceConcurrently("dup@example.com");

		assertThat(rowCount()).isEqualTo(2);
		assertThat(results).containsOnly(Boolean.TRUE);
	}

	@Test
	@DisplayName("UNIQUE가 있으면 DB가 마지막 방어선이 되어 한 요청의 INSERT만 성공한다")
	void uniqueConstraintIsTheLastLineOfDefense() throws Exception {
		createTable(true);

		List<Object> results = signUpTwiceConcurrently("dup@example.com");

		assertThat(rowCount()).isEqualTo(1);
		assertThat(results)
				.filteredOn(DataIntegrityViolationException.class::isInstance)
				.hasSize(1);
	}

	private void createTable(boolean unique) {
		jdbcTemplate.execute("drop table if exists " + TABLE);
		jdbcTemplate.execute("create table " + TABLE
				+ " (id bigint primary key, email varchar(255) not null"
				+ (unique ? ", unique key uk_email (email)" : "") + ")");
	}

	private List<Object> signUpTwiceConcurrently(String email) throws Exception {
		CyclicBarrier bothHaveChecked = new CyclicBarrier(2);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<Future<Object>> futures = new ArrayList<>();
			for (long id = 1; id <= 2; id++) {
				long memberId = id;
				futures.add(executor.submit(() -> naiveSignUp(memberId, email, bothHaveChecked)));
			}
			List<Object> results = new ArrayList<>();
			for (Future<Object> future : futures) {
				results.add(future.get(10, TimeUnit.SECONDS));
			}
			return results;
		} finally {
			executor.shutdownNow();
		}
	}

	/** 안티패턴: 조회와 INSERT 사이에 다른 요청이 끼어들 수 있다. */
	private Object naiveSignUp(long id, String email, CyclicBarrier bothHaveChecked)
			throws Exception {
		Integer existing = jdbcTemplate.queryForObject(
				"select count(*) from " + TABLE + " where email = ?", Integer.class, email);
		if (existing != null && existing > 0) {
			return Boolean.FALSE;
		}
		bothHaveChecked.await(5, TimeUnit.SECONDS); // 두 요청이 모두 "없음"을 본 시점
		try {
			jdbcTemplate.update("insert into " + TABLE + " (id, email) values (?, ?)", id, email);
			return Boolean.TRUE;
		} catch (DataIntegrityViolationException e) {
			return e;
		}
	}

	private Integer rowCount() {
		return jdbcTemplate.queryForObject("select count(*) from " + TABLE, Integer.class);
	}
}
