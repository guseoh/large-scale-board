package io.github.guseoh.board.id;

import java.time.Instant;

/**
 * Snowflake ID의 비트 구성. 조합과 분해만 하고 상태를 갖지 않는다. (D0-09)
 *
 * <pre>
 * | 부호 1비트(0) | 시각 41비트(ms) | 노드 ID 10비트 | 순번 12비트 |
 * </pre>
 *
 * <p>생성기({@link SnowflakeIdGenerator})와 분리한 이유: 데이터 생성기(datagen)는 "과거 시각 + 노드
 * 1023 + 순번"으로 ID를 직접 조합해야 한다. 현재 시각을 쓰는 생성기로는 과거 데이터를 만들 수 없다.
 */
public final class Snowflake {

	/** 기준 시각 2026-01-01T00:00:00Z. 41비트로 이 시각부터 약 69.7년을 쓸 수 있다. */
	public static final long EPOCH_MILLIS = 1_767_225_600_000L;

	public static final int TIMESTAMP_BITS = 41;
	public static final int NODE_ID_BITS = 10;
	public static final int SEQUENCE_BITS = 12;

	public static final long MAX_ELAPSED_MILLIS = (1L << TIMESTAMP_BITS) - 1;
	public static final int MAX_NODE_ID = (1 << NODE_ID_BITS) - 1;    // 1023
	public static final int MAX_SEQUENCE = (1 << SEQUENCE_BITS) - 1;  // 4095

	private static final int NODE_ID_SHIFT = SEQUENCE_BITS;                 // 12
	private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + NODE_ID_BITS; // 22

	private Snowflake() {
	}

	/**
	 * 시각, 노드 ID, 순번을 ID 하나로 조합한다.
	 *
	 * @param epochMillis Unix 기준 밀리초. {@link #EPOCH_MILLIS}보다 작거나 41비트를 넘으면 예외
	 */
	public static long compose(long epochMillis, int nodeId, int sequence) {
		long elapsed = epochMillis - EPOCH_MILLIS;
		if (elapsed < 0 || elapsed > MAX_ELAPSED_MILLIS) {
			throw new IllegalArgumentException(
					"시각이 Snowflake 범위를 벗어났습니다: " + Instant.ofEpochMilli(epochMillis));
		}
		requireNodeId(nodeId);
		if (sequence < 0 || sequence > MAX_SEQUENCE) {
			throw new IllegalArgumentException(
					"순번은 0~" + MAX_SEQUENCE + "이어야 합니다: " + sequence);
		}
		return elapsed << TIMESTAMP_SHIFT | (long) nodeId << NODE_ID_SHIFT | sequence;
	}

	/** ID를 만든 시각(Unix 기준 밀리초). */
	public static long epochMillisOf(long id) {
		return (id >>> TIMESTAMP_SHIFT) + EPOCH_MILLIS;
	}

	public static int nodeIdOf(long id) {
		return (int) (id >>> NODE_ID_SHIFT) & MAX_NODE_ID;
	}

	public static int sequenceOf(long id) {
		return (int) id & MAX_SEQUENCE;
	}

	static int requireNodeId(int nodeId) {
		if (nodeId < 0 || nodeId > MAX_NODE_ID) {
			throw new IllegalArgumentException(
					"노드 ID는 0~" + MAX_NODE_ID + "이어야 합니다: " + nodeId);
		}
		return nodeId;
	}
}
