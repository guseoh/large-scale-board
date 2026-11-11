package io.github.guseoh.datagen.table;

import io.github.guseoh.board.id.Snowflake;
import io.github.guseoh.datagen.core.Seeds;
import io.github.guseoh.datagen.core.SnowflakeTimeline;
import io.github.guseoh.datagen.core.TextGenerator;
import io.github.guseoh.datagen.core.ZipfSampler;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SplittableRandom;

/**
 * {@code exp_load_article} 행을 PK 순서로 하나씩 만든다. 같은 시드와 같은 행 수는 언제나 같은 행을
 * 만든다.
 *
 * <p>결정성을 지키는 규칙 (Step-0-Design 11절)
 * <ul>
 *   <li>난수는 이 테이블 전용 {@link SplittableRandom} 하나에서만 얻는다.</li>
 *   <li>현재 시각을 쓰지 않는다. 생성 시각은 고정된 기준 시각에서 과거 {@code spanDays}일 안에서
 *       만든다. 설계는 365일이지만 Snowflake 기준 시각(2026-01-01) 이전은 ID로 표현할 수 없어서
 *       기본값을 300일로 둔다.</li>
 *   <li>한 행 안에서 난수를 꺼내는 순서를 바꾸지 않는다. 순서를 바꾸면 같은 시드라도 다른 데이터가
 *       된다.</li>
 * </ul>
 */
public final class ExpLoadArticleGenerator implements Iterator<ExpLoadArticleRow> {

	public static final String TABLE = "exp_load_article";

	/** 게시판 수. 게시판별 글 수는 Zipf 분포를 따른다(1번 게시판이 가장 많다). */
	public static final int BOARDS = 100;
	public static final double BOARD_ZIPF_EXPONENT = 1.0;
	public static final int WRITERS = 1_000_000;

	private final long rows;
	private final SplittableRandom random;
	private final SnowflakeTimeline timeline;
	private final ZipfSampler boardSampler = new ZipfSampler(BOARDS, BOARD_ZIPF_EXPONENT);

	private long produced;

	public ExpLoadArticleGenerator(long seed, long rows, Instant baseTime, int spanDays) {
		this.rows = rows;
		this.random = Seeds.forTable(seed, TABLE);
		long end = baseTime.toEpochMilli();
		long start = baseTime.minus(Duration.ofDays(spanDays)).toEpochMilli();
		// Snowflake는 기준 시각(2026-01-01) 이전을 표현하지 못한다. 기간이 그 앞으로 넘어가면
		// 처음부터 막는다.
		if (start < Snowflake.EPOCH_MILLIS) {
			throw new IllegalArgumentException("생성 기간 시작(" + Instant.ofEpochMilli(start)
					+ ")이 Snowflake 기준 시각(2026-01-01)보다 앞입니다. "
					+ "base-time을 늦추거나 span-days를 줄이세요.");
		}
		this.timeline = new SnowflakeTimeline(start, end, rows);
	}

	@Override
	public boolean hasNext() {
		return produced < rows;
	}

	@Override
	public ExpLoadArticleRow next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		produced++;
		// 아래 순서가 곧 데이터다. 바꾸지 않는다.
		long id = timeline.nextId(random);
		long boardId = boardSampler.sample(random);
		long writerId = random.nextLong(1, WRITERS + 1);
		String title = TextGenerator.words(random, 2, 8);
		String content = TextGenerator.words(random, 20, 150);
		LocalDateTime createdAt = LocalDateTime.ofEpochSecond(
				Math.floorDiv(timeline.lastMillis(), 1000L),
				(int) Math.floorMod(timeline.lastMillis(), 1000L) * 1_000_000,
				ZoneOffset.UTC);
		return new ExpLoadArticleRow(id, boardId, writerId, title, content, createdAt);
	}
}
