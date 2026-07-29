package io.github.guseoh.board;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void MySQL에_연결하고_쿼리를_실행한다() {
        Integer result = jdbcTemplate.queryForObject(
                "select 1",
                Integer.class
        );

        assertThat(result).isEqualTo(1);
    }
}