package io.github.guseoh.datagen.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guseoh.board.id.Snowflake;
import io.github.guseoh.datagen.core.Seeds;
import io.github.guseoh.datagen.csv.CsvExporter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ExpLoadArticleGeneratorTest {

	private static final Instant BASE = Instant.parse("2026-11-01T00:00:00Z");
	private static final int SPAN_DAYS = 300;

	@Test
	void 같은_시드로_만든_CSV는_SHA256이_같다() {
		CsvExporter.Result first =
				CsvExporter.write(generator(42, 10_000), new ByteArrayOutputStream());
		CsvExporter.Result second =
				CsvExporter.write(generator(42, 10_000), new ByteArrayOutputStream());

		assertThat(first.rows()).isEqualTo(10_000);
		assertThat(second.sha256()).isEqualTo(first.sha256());
		assertThat(second.bytes()).isEqualTo(first.bytes());
	}

	@Test
	void 시드가_다르면_CSV가_다르다() {
		CsvExporter.Result a =
				CsvExporter.write(generator(42, 10_000), new ByteArrayOutputStream());
		CsvExporter.Result b =
				CsvExporter.write(generator(43, 10_000), new ByteArrayOutputStream());

		assertThat(b.sha256()).isNotEqualTo(a.sha256());
	}

	@Test
	void 테이블마다_하위_시드가_다르다() {
		assertThat(Seeds.subSeed(42, "exp_load_article")).isNotEqualTo(Seeds.subSeed(42, "member"));
		assertThat(Seeds.subSeed(42, "exp_load_article"))
				.isEqualTo(Seeds.subSeed(42, "exp_load_article"));
	}

	@Test
	void ID는_PK_순서로_증가하고_노드_1023이며_생성_시각은_기간_안이다() {
		ExpLoadArticleGenerator generator = generator(7, 50_000);
		long previousId = Long.MIN_VALUE;
		long start = BASE.minus(Duration.ofDays(SPAN_DAYS)).toEpochMilli();
		long end = BASE.toEpochMilli();

		while (generator.hasNext()) {
			ExpLoadArticleRow row = generator.next();
			long createdMillis = row.createdAt().toInstant(ZoneOffset.UTC).toEpochMilli();

			assertThat(row.id()).isGreaterThan(previousId);
			assertThat(Snowflake.nodeIdOf(row.id())).isEqualTo(1023);
			assertThat(Snowflake.epochMillisOf(row.id())).isEqualTo(createdMillis);
			assertThat(createdMillis).isBetween(start, end);
			assertThat(row.boardId()).isBetween(1L, (long) ExpLoadArticleGenerator.BOARDS);
			assertThat(row.writerId()).isBetween(1L, (long) ExpLoadArticleGenerator.WRITERS);
			previousId = row.id();
		}
	}

	@Test
	void 생성_시각이_기간_전체에_고르게_퍼진다() {
		ExpLoadArticleGenerator generator = generator(7, 100_000);
		long start = BASE.minus(Duration.ofDays(SPAN_DAYS)).toEpochMilli();
		long span = BASE.toEpochMilli() - start;
		int[] quarters = new int[4];

		while (generator.hasNext()) {
			long millis = generator.next().createdAt().toInstant(ZoneOffset.UTC).toEpochMilli();
			quarters[(int) Math.min(3, (millis - start) * 4 / span)]++;
		}

		// 지수 간격을 더해 만든 시각이 기간 끝까지 고르게 닿는지 본다(분기마다 약 25%).
		for (int q : quarters) {
			assertThat(q).isBetween(23_000, 27_000);
		}
	}

	@Test
	void CSV_값에는_구분자와_줄바꿈이_들어가지_않는다() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CsvExporter.write(generator(1, 2_000), out);

		for (String line : out.toString(StandardCharsets.UTF_8).split("\n")) {
			assertThat(line.split(",", -1)).hasSize(6);
			assertThat(line).doesNotContain("\"").doesNotContain("\r");
		}
	}

	@Test
	void 생성_기간이_Snowflake_기준_시각보다_앞이면_거부한다() {
		assertThatThrownBy(() -> new ExpLoadArticleGenerator(1, 10, BASE, 365))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("2026-01-01");
	}

	private static ExpLoadArticleGenerator generator(long seed, long rows) {
		return new ExpLoadArticleGenerator(seed, rows, BASE, SPAN_DAYS);
	}
}
