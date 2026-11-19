package io.github.guseoh.board.probe;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guseoh.board.TestcontainersConfiguration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

/**
 * 탐침: JVM 기본 시간대가 KST일 때 시각 타입별로 DB에 어떤 값이 들어가고 어떻게 읽히는지 고정한다.
 * 결론은 ADR-008(발생 시각은 Instant)이다. 1-A가 끝나면 지운다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class TimeZoneProbeTest {

	private static final TimeZone ORIGINAL = TimeZone.getDefault();
	private static final TimeZone KST = TimeZone.getTimeZone("Asia/Seoul");
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	private final TimeProbeRepository repository;
	private final JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("delete from time_probe");
		TimeZone.setDefault(KST); // 개발 PC 상황
	}

	@AfterEach
	void tearDown() {
		TimeZone.setDefault(ORIGINAL); // JVM 전역 상태라 반드시 복원한다
	}

	@Test
	@DisplayName("Instant는 JVM 시간대와 무관하게 UTC 값 그대로 저장된다")
	void instantIsStoredAsUtc() {
		repository.save(new TimeProbe(1L, null, Instant.parse("2026-11-01T00:00:00Z")));

		assertThat(raw("instant")).isEqualTo("2026-11-01 00:00:00.000000");
	}

	@Test
	@DisplayName("함정: LocalDateTime은 KST JVM에서 9시간 앞선 UTC 값으로 저장된다")
	void localDateTimeIsShiftedByJvmZone() {
		repository.save(new TimeProbe(2L, LocalDateTime.of(2026, 11, 1, 0, 0), null));

		assertThat(raw("local_dt")).isEqualTo("2026-10-31 15:00:00.000000");
	}

	@Test
	@DisplayName("함정: KST에서 쓰고 KST에서 읽으면 변환이 상쇄되어 정상처럼 보인다")
	void localDateTimeLooksCorrectInSameZone() {
		repository.save(new TimeProbe(3L, LocalDateTime.of(2026, 11, 1, 0, 0), null));

		assertThat(repository.findById(3L).orElseThrow().getLocalDt())
				.isEqualTo(LocalDateTime.of(2026, 11, 1, 0, 0));
	}

	@Test
	@DisplayName("함정: 같은 행을 UTC JVM에서 읽으면 LocalDateTime은 9시간 어긋난다")
	void localDateTimeBreaksInOtherZone() {
		repository.save(new TimeProbe(4L, LocalDateTime.of(2026, 11, 1, 0, 0), null));

		TimeZone.setDefault(UTC); // 서버가 UTC인 상황

		assertThat(repository.findById(4L).orElseThrow().getLocalDt())
				.isEqualTo(LocalDateTime.of(2026, 10, 31, 15, 0));
	}

	@Test
	@DisplayName("Instant는 어느 시간대의 JVM에서 읽어도 같다")
	void instantIsSameInAnyZone() {
		Instant instant = Instant.parse("2026-11-01T00:00:00Z");
		repository.save(new TimeProbe(5L, null, instant));

		TimeZone.setDefault(UTC);

		assertThat(repository.findById(5L).orElseThrow().getInstant()).isEqualTo(instant);
	}

	@Test
	@DisplayName("DATETIME(6)은 나노초를 마이크로초로 반올림해 저장한다")
	void instantIsRoundedToMicros() {
		repository.save(new TimeProbe(6L, null, Instant.parse("2026-11-01T00:00:00.123456789Z")));

		assertThat(raw("instant")).isEqualTo("2026-11-01 00:00:00.123457");
	}

	private String raw(String column) {
		// JPA를 거치지 않고 DB에 실제로 들어간 값을 문자열로 읽는다.
		return jdbcTemplate.queryForObject(
				"select cast(" + column + " as char) from time_probe", String.class);
	}
}
