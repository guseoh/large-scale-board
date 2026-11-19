package io.github.guseoh.board.common.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guseoh.board.TestcontainersConfiguration;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

/**
 * 문제 재현: 직접 할당한 ID를 save()하면 INSERT 앞에 SELECT가 한 번 더 나간다(0단계 H-01 A′의 원인).
 * 이 테스트는 "지금 동작"을 고정한다. BaseEntity가 이 문제를 어떻게 없애는지는 BaseEntityTest가 본다.
 */
@SpringBootTest(properties = {
		"spring.jpa.properties.hibernate.generate_statistics=true",
		"logging.level.org.hibernate.SQL=DEBUG"
})
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class AssignedIdSaveCostTest {

	private final PlainProbeRepository repository;
	private final EntityManagerFactory entityManagerFactory;

	private Statistics statistics;

	@BeforeEach
	void setUp() {
		repository.deleteAllInBatch();
		statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		statistics.clear();
	}

	@Test
	@DisplayName("함정: 직접 할당 ID를 save()하면 SELECT 후 INSERT로 SQL이 2번 나간다")
	void saveWithAssignedIdIssuesSelectBeforeInsert() {
		repository.save(new PlainProbe(1L, "a"));

		assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
	}
}
