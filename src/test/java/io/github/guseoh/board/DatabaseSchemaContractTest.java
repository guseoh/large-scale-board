package io.github.guseoh.board;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DatabaseSchemaContractTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void Flyway_V1_마이그레이션이_적용된다() {
        MigrationInfo current = flyway.info().current();

        assertThat(current).isNotNull();
        assertThat(current.getVersion().getVersion()).isEqualTo("2");
        assertThat(current.getDescription()).isEqualTo("create members");
    }

    @Test
    void Flyway_이력_테이블에_성공_결과가_기록된다() {
        Integer appliedCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '1'
                  AND success = 1
                """,
                Integer.class
        );

        assertThat(appliedCount).isEqualTo(1);
    }
}