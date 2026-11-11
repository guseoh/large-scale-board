package io.github.guseoh.board.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SnowflakeTest {

	@Test
	void 기준_시각은_2026년_1월_1일_UTC다() {
		assertThat(Snowflake.EPOCH_MILLIS)
				.isEqualTo(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli());
	}

	@Test
	void 조합한_ID를_분해하면_넣은_값이_그대로_나온다() {
		long millis = Instant.parse("2026-11-11T12:34:56.789Z").toEpochMilli();

		long id = Snowflake.compose(millis, 7, 42);

		assertThat(Snowflake.epochMillisOf(id)).isEqualTo(millis);
		assertThat(Snowflake.nodeIdOf(id)).isEqualTo(7);
		assertThat(Snowflake.sequenceOf(id)).isEqualTo(42);
	}

	@Test
	void 모든_필드가_최대값이어도_부호_비트는_0이다() {
		long maxMillis = Snowflake.EPOCH_MILLIS + Snowflake.MAX_ELAPSED_MILLIS;

		long id = Snowflake.compose(maxMillis, Snowflake.MAX_NODE_ID, Snowflake.MAX_SEQUENCE);

		assertThat(id).isEqualTo(Long.MAX_VALUE).isPositive();
		assertThat(Snowflake.nodeIdOf(id)).isEqualTo(1023);
		assertThat(Snowflake.sequenceOf(id)).isEqualTo(4095);
	}

	@Test
	void 지금_만든_ID는_JavaScript_안전_정수_범위를_넘는다() {
		// D0-11: 그래서 API 응답에서는 ID를 문자열로 내보낸다
		long id = Snowflake.compose(Instant.parse("2026-11-11T00:00:00Z").toEpochMilli(), 0, 0);

		assertThat(id).isGreaterThan((1L << 53) - 1);
	}

	@Test
	void 범위를_벗어난_값은_거부한다() {
		long now = Instant.parse("2026-11-11T00:00:00Z").toEpochMilli();

		assertThatThrownBy(() -> Snowflake.compose(Snowflake.EPOCH_MILLIS - 1, 0, 0))
				.isInstanceOf(IllegalArgumentException.class);
		long afterLimit = Snowflake.EPOCH_MILLIS + Snowflake.MAX_ELAPSED_MILLIS + 1;
		assertThatThrownBy(() -> Snowflake.compose(afterLimit, 0, 0))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Snowflake.compose(now, -1, 0))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Snowflake.compose(now, 1024, 0))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Snowflake.compose(now, 0, -1))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Snowflake.compose(now, 0, 4096))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
