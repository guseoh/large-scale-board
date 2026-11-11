package io.github.guseoh.board.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;

class SnowflakeIdGeneratorTest {

	private static final long T = Instant.parse("2026-11-11T00:00:00Z").toEpochMilli();

	@Test
	void 노드_ID는_0부터_1023까지만_허용한다() {
		assertThat(new SnowflakeIdGenerator(0).nodeId()).isZero();
		assertThat(new SnowflakeIdGenerator(1023).nodeId()).isEqualTo(1023);
		assertThatThrownBy(() -> new SnowflakeIdGenerator(-1))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new SnowflakeIdGenerator(1024))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 같은_밀리초에서는_순번이_오르고_시각이_바뀌면_0으로_돌아간다() {
		SnowflakeIdGenerator generator =
				new SnowflakeIdGenerator(7, new ScriptedClock(T, T, T + 1));

		long first = generator.nextId();
		long second = generator.nextId();
		long third = generator.nextId();

		assertThat(Snowflake.epochMillisOf(first)).isEqualTo(T);
		assertThat(Snowflake.nodeIdOf(first)).isEqualTo(7);
		assertThat(Snowflake.sequenceOf(first)).isZero();
		assertThat(Snowflake.sequenceOf(second)).isEqualTo(1);
		assertThat(Snowflake.epochMillisOf(third)).isEqualTo(T + 1);
		assertThat(Snowflake.sequenceOf(third)).isZero();
	}

	@Test
	void 순번_4096개를_다_쓰면_다음_밀리초로_넘어간다() {
		// 4,096번째까지는 같은 밀리초 T, 4,097번째 요청 때도 아직 T, 기다리는 동안 한 번 더 T를 본
		// 뒤 T+1이 된다
		long[] script = new long[4096 + 3];
		Arrays.fill(script, T);
		script[script.length - 1] = T + 1;
		SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, new ScriptedClock(script));

		long last = 0;
		for (int i = 0; i < 4096; i++) {
			last = generator.nextId();
		}
		long overflowed = generator.nextId();

		assertThat(Snowflake.epochMillisOf(last)).isEqualTo(T);
		assertThat(Snowflake.sequenceOf(last)).isEqualTo(4095);
		assertThat(Snowflake.epochMillisOf(overflowed)).isEqualTo(T + 1);
		assertThat(Snowflake.sequenceOf(overflowed)).isZero();
		assertThat(overflowed).isGreaterThan(last);
	}

	@Test
	void 시계가_5ms_이내로_뒤로_가면_따라잡을_때까지_기다린다() {
		SnowflakeIdGenerator generator =
				new SnowflakeIdGenerator(1, new ScriptedClock(T, T - 3, T - 2, T - 1, T));

		long before = generator.nextId();
		long after = generator.nextId();

		assertThat(after).isGreaterThan(before);
		assertThat(Snowflake.epochMillisOf(after)).isEqualTo(T);
		assertThat(Snowflake.sequenceOf(after)).isEqualTo(1);
	}

	@Test
	void 시계가_5ms보다_크게_뒤로_가면_예외를_던진다() {
		SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, new ScriptedClock(T, T - 6));

		generator.nextId();

		assertThatThrownBy(generator::nextId)
				.isInstanceOf(ClockMovedBackwardsException.class)
				.hasMessageContaining("6ms");
	}

	@Test
	void 실제_시계로_발급한_ID는_단조_증가한다() {
		SnowflakeIdGenerator generator = new SnowflakeIdGenerator(3);

		long previous = generator.nextId();
		for (int i = 0; i < 100_000; i++) {
			long current = generator.nextId();
			assertThat(current).isGreaterThan(previous);
			previous = current;
		}
	}

	@Test
	void 스레드_32개가_10만_개씩_발급해도_중복이_없다() throws Exception {
		int threads = 32;
		int perThread = 100_000;
		SnowflakeIdGenerator generator = new SnowflakeIdGenerator(5);
		CountDownLatch start = new CountDownLatch(1);

		List<Future<long[]>> futures = new ArrayList<>();
		try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
			for (int t = 0; t < threads; t++) {
				futures.add(executor.submit(() -> {
					long[] ids = new long[perThread];
					start.await(); // 모든 스레드가 준비된 뒤 동시에 출발해 경합을 최대로 만든다
					for (int i = 0; i < perThread; i++) {
						ids[i] = generator.nextId();
					}
					return ids;
				}));
			}
			start.countDown();

			long[] all = new long[threads * perThread];
			int offset = 0;
			for (Future<long[]> future : futures) {
				long[] ids = future.get();
				// 한 스레드가 받은 ID는 받은 순서대로 증가해야 한다(생성기 하나 안에서 단조 증가)
				for (int i = 1; i < ids.length; i++) {
					assertThat(ids[i]).isGreaterThan(ids[i - 1]);
				}
				System.arraycopy(ids, 0, all, offset, ids.length);
				offset += ids.length;
			}

			// 3,200,000개를 Set에 넣으면 박싱으로 수백 MB를 쓴다. 정렬 후 이웃끼리 비교한다.
			Arrays.sort(all);
			for (int i = 1; i < all.length; i++) {
				if (all[i] == all[i - 1]) {
					throw new AssertionError("중복 ID: " + all[i]);
				}
			}
			assertThat(all).hasSize(3_200_000);
		}
	}

	/** 미리 정한 값을 차례로 돌려주는 시계. 값을 다 쓰면 마지막 값을 계속 돌려준다. */
	private static final class ScriptedClock implements LongSupplier {

		private final long[] values;
		private int index;

		ScriptedClock(long... values) {
			this.values = values;
		}

		@Override
		public long getAsLong() {
			long value = values[Math.min(index, values.length - 1)];
			index++;
			return value;
		}
	}
}
