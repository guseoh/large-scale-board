package io.github.guseoh.board.common.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guseoh.board.TestcontainersConfiguration;
import jakarta.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

/**
 * BaseEntity 계약: 직접 할당 ID에서 불필요한 SELECT가 없고, 생성·수정 시각이 Clock 기준 UTC Instant로 채워진다.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({TestcontainersConfiguration.class, BaseEntityTest.ClockConfig.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class BaseEntityTest {

	private static final Instant T1 = Instant.parse("2026-11-01T00:00:00Z");

	private final BaseEntityProbeRepository repository;
	private final JdbcTemplate jdbcTemplate;
	private final MutableClock clock;
	private final EntityManagerFactory entityManagerFactory;

	private Statistics statistics;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("delete from base_entity_probe");
		clock.set(T1);
		statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		statistics.clear();
	}

	@Test
	@DisplayName("새 엔티티를 save()하면 SELECT 없이 INSERT 한 번만 나간다")
	void saveNewEntityIssuesOnlyInsert() {
		repository.save(new BaseEntityProbe(1L, "a"));

		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("생성 직후에는 새 엔티티이고, 저장하면 새 엔티티가 아니다")
	void isNewBecomesFalseAfterSave() {
		BaseEntityProbe entity = new BaseEntityProbe(2L, "a");
		assertThat(entity.isNew()).isTrue();

		repository.save(entity);

		assertThat(entity.isNew()).isFalse();
	}

	@Test
	@DisplayName("DB에서 읽어 온 엔티티는 새 엔티티가 아니다")
	void loadedEntityIsNotNew() {
		repository.save(new BaseEntityProbe(3L, "a"));

		BaseEntityProbe loaded = repository.findById(3L).orElseThrow();

		assertThat(loaded.isNew()).isFalse();
	}

	@Test
	@DisplayName("저장하면 createdAt과 modifiedAt이 Clock의 현재 시각으로 채워진다")
	void timestampsAreFilledFromClockOnInsert() {
		repository.save(new BaseEntityProbe(4L, "a"));

		BaseEntityProbe loaded = repository.findById(4L).orElseThrow();

		assertThat(loaded.getCreatedAt()).isEqualTo(T1);
		assertThat(loaded.getModifiedAt()).isEqualTo(T1);
	}

	@Test
	@DisplayName("JVM 시간대가 KST여도 시각은 UTC 값으로 저장된다")
	void timestampsAreStoredAsUtcRegardlessOfJvmZone() {
		TimeZone original = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		try {
			repository.save(new BaseEntityProbe(5L, "a"));
		} finally {
			TimeZone.setDefault(original);
		}

		String raw = jdbcTemplate.queryForObject(
				"select cast(created_at as char) from base_entity_probe where id = 5", String.class);
		assertThat(raw).isEqualTo("2026-11-01 00:00:00.000000");
	}

	@Test
	@DisplayName("수정하면 modifiedAt만 바뀌고 createdAt은 그대로다")
	void updateChangesOnlyModifiedAt() {
		repository.save(new BaseEntityProbe(6L, "a"));
		Instant t2 = T1.plus(Duration.ofMinutes(5));
		clock.set(t2);

		BaseEntityProbe loaded = repository.findById(6L).orElseThrow();
		loaded.rename("b");
		repository.save(loaded);

		BaseEntityProbe updated = repository.findById(6L).orElseThrow();
		assertThat(updated.getName()).isEqualTo("b");
		assertThat(updated.getCreatedAt()).isEqualTo(T1);
		assertThat(updated.getModifiedAt()).isEqualTo(t2);
	}

	@Test
	@DisplayName("시각은 마이크로초로 잘라 저장하므로 저장한 값과 다시 읽은 값이 같다")
	void timestampsAreTruncatedToMicros() {
		clock.set(Instant.parse("2026-11-01T00:00:00.123456789Z"));

		BaseEntityProbe saved = repository.save(new BaseEntityProbe(7L, "a"));
		BaseEntityProbe reloaded = repository.findById(7L).orElseThrow();

		Instant truncated = Instant.parse("2026-11-01T00:00:00.123456Z");
		assertThat(saved.getCreatedAt()).isEqualTo(truncated);
		assertThat(reloaded.getCreatedAt()).isEqualTo(truncated);
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class ClockConfig {

		@Bean
		@Primary
		MutableClock testClock() {
			return new MutableClock(T1);
		}
	}
}
