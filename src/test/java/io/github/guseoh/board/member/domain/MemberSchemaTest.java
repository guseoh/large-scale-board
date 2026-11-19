package io.github.guseoh.board.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guseoh.board.TestcontainersConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

/**
 * 직접 쓴 DDL(db/schema/member.sql)이 Hibernate(ddl-auto=create)가 만든 member 테이블과 같은지 비교한다.
 * 같은 DB에 member_ddl_check라는 이름으로 직접 쓴 DDL을 만들어 information_schema를 나란히 읽는다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class MemberSchemaTest {

	private static final String CHECK_TABLE = "member_ddl_check";

	private final JdbcTemplate jdbcTemplate;

	@AfterEach
	void tearDown() {
		jdbcTemplate.execute("drop table if exists " + CHECK_TABLE);
	}

	@Test
	@DisplayName("직접 쓴 DDL과 Hibernate가 만든 DDL의 컬럼·인덱스·콜레이션이 같다")
	void handWrittenDdlMatchesHibernateSchema() throws IOException {
		String ddl = Files.readString(Path.of("db/schema/member.sql"))
				.replaceFirst("CREATE TABLE member\\b", "CREATE TABLE " + CHECK_TABLE);
		jdbcTemplate.execute("drop table if exists " + CHECK_TABLE);
		jdbcTemplate.execute(ddl);

		assertThat(describe(CHECK_TABLE)).containsExactlyElementsOf(describe("member"));
	}

	private List<String> describe(String table) {
		List<String> columns = jdbcTemplate.queryForList("""
				select concat('column ', column_name, ' ', column_type, ' null=', is_nullable,
				              ' collation=', coalesce(collation_name, '-'))
				from information_schema.columns
				where table_schema = database() and table_name = ?
				order by column_name
				""", String.class, table);
		List<String> indexes = jdbcTemplate.queryForList("""
				select concat('index ', index_name, ' unique=', 1 - non_unique, ' (',
				              group_concat(column_name order by seq_in_index), ')')
				from information_schema.statistics
				where table_schema = database() and table_name = ?
				group by index_name, non_unique
				order by index_name
				""", String.class, table);
		List<String> tables = jdbcTemplate.queryForList("""
				select concat('table engine=', engine, ' collation=', table_collation)
				from information_schema.tables
				where table_schema = database() and table_name = ?
				""", String.class, table);
		return java.util.stream.Stream.of(columns, indexes, tables).flatMap(List::stream).toList();
	}
}
