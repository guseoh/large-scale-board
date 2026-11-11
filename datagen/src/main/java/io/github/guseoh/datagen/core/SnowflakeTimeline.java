package io.github.guseoh.datagen.core;

import io.github.guseoh.board.id.Snowflake;
import java.util.SplittableRandom;

/**
 * 과거 기간 안에서 시간순으로 증가하는 생성 시각과 Snowflake ID를 하나씩 만든다.
 *
 * <p>왜 정렬하지 않고 차례로 만드는가: 행 N개의 시각을 무작위로 뽑아 정렬하면 N개를 메모리에 올려야
 * 한다(5,000만 건이면 400MB). 대신 "다음 행까지의 간격"을 지수 분포로 뽑아 더해 가면, 메모리 없이
 * 증가하는 시각을 만들 수 있다. 사건이 무작위로 일어나는 과정(포아송 과정)의 간격이 지수 분포를
 * 따르기 때문에, 결과는 기간 안에 고르게 흩어진 시각과 비슷하다.
 *
 * <p>ID는 실제 Snowflake와 같은 모양이다: 생성 시각 + 노드 1023(datagen 전용) + 같은 밀리초 안의
 * 순번. 시간순으로 증가하므로 PK 순서로 CSV를 쓸 수 있고, PK 순서로 넣어야 InnoDB 페이지 분할이
 * 줄어든다.
 */
public final class SnowflakeTimeline {

	/** datagen 전용 노드 ID. 앱 인스턴스는 이 값을 쓰지 않는다. (D0-10) */
	public static final int DATAGEN_NODE_ID = Snowflake.MAX_NODE_ID;

	private final long endMillis;
	private final double meanGapMillis;

	private double currentMillis;
	private long lastMillis = -1;
	private int sequence;

	private long lastId;

	/**
	 * @param startMillis 기간 시작(포함)
	 * @param endMillis   기간 끝. 마지막 행들이 끝을 넘으면 끝 시각에 붙인다.
	 * @param rows        만들 행 수. 평균 간격 = 기간 ÷ 행 수
	 */
	public SnowflakeTimeline(long startMillis, long endMillis, long rows) {
		if (endMillis <= startMillis || rows < 1) {
			throw new IllegalArgumentException("기간과 행 수가 올바르지 않습니다");
		}
		this.endMillis = endMillis;
		this.meanGapMillis = (double) (endMillis - startMillis) / rows;
		this.currentMillis = startMillis;
	}

	/** 다음 행의 시각을 정하고 ID를 돌려준다. 시각은 {@link #lastMillis()}로 읽는다. */
	public long nextId(SplittableRandom random) {
		// 지수 분포 간격: -평균 × ln(1 - u). u는 [0, 1)이므로 1 - u는 0이 되지 않는다.
		currentMillis += -meanGapMillis * Math.log(1 - random.nextDouble());
		long millis = Math.min((long) currentMillis, endMillis);

		if (millis <= lastMillis) {
			millis = lastMillis;
			sequence++;
			if (sequence > Snowflake.MAX_SEQUENCE) {
				millis++;
				sequence = 0;
			}
		} else {
			sequence = 0;
		}
		lastMillis = millis;
		lastId = Snowflake.compose(millis, DATAGEN_NODE_ID, sequence);
		return lastId;
	}

	public long lastMillis() {
		return lastMillis;
	}

	public long lastId() {
		return lastId;
	}
}
