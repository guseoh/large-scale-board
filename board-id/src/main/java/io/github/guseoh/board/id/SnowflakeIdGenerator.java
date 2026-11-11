package io.github.guseoh.board.id;

import java.util.function.LongSupplier;

/**
 * Snowflake ID 생성기. 인스턴스 하나가 노드 하나다. 스레드에 안전하다. (D0-09, D0-10)
 *
 * <p>같은 노드 ID로 생성기를 둘 이상 띄우면 중복 ID가 나온다. 이것이 이 방식의 실패 지점이다. 앱
 * 인스턴스가 2대가 되는 4단계부터는 인스턴스마다 다른 노드 ID를 준다.
 */
public class SnowflakeIdGenerator {

	/** 이 정도 역행은 NTP 보정에서 흔하므로 따라잡을 때까지 기다린다. 그보다 크면 예외를 던진다. */
	static final long MAX_BACKWARD_MILLIS = 5;

	private final int nodeId;
	private final LongSupplier clock;

	// 두 값을 항상 함께 읽고 바꾸므로 같은 락(this)으로 보호한다.
	private long lastMillis = -1;
	private int sequence;

	public SnowflakeIdGenerator(int nodeId) {
		this(nodeId, System::currentTimeMillis);
	}

	/**
	 * @param clock 현재 시각(Unix 기준 밀리초) 공급 함수. 테스트에서 시계 역행과 순번 초과를
	 *     재현하려고 주입받는다.
	 */
	public SnowflakeIdGenerator(int nodeId, LongSupplier clock) {
		this.nodeId = Snowflake.requireNodeId(nodeId);
		this.clock = clock;
	}

	/**
	 * 다음 ID를 만든다.
	 *
	 * <p>{@code synchronized}를 쓰는 이유: 시각과 순번 두 값을 한 번에 바꿔야 하고, 임계 구역이 몇
	 * 줄뿐이라 경합 비용이 작다. 대안은 두 값을 {@code long} 하나에 묶어 CAS로 갱신하는 방식이다.
	 * 스레드 수별 발급 속도를 재 보고 병목일 때만 바꾼다.
	 *
	 * @throws ClockMovedBackwardsException 시계가 {@value #MAX_BACKWARD_MILLIS}ms보다 크게 뒤로
	 *     갔을 때
	 */
	public synchronized long nextId() {
		long now = clock.getAsLong();

		if (now < lastMillis) {
			if (lastMillis - now > MAX_BACKWARD_MILLIS) {
				throw new ClockMovedBackwardsException(lastMillis, now);
			}
			now = waitUntil(lastMillis);
		}

		if (now == lastMillis) {
			sequence = (sequence + 1) & Snowflake.MAX_SEQUENCE;
			if (sequence == 0) {
				// 이 밀리초의 순번 4,096개를 다 썼다. 다음 밀리초가 될 때까지 기다린다.
				now = waitUntil(lastMillis + 1);
			}
		} else {
			sequence = 0;
		}

		lastMillis = now;
		return Snowflake.compose(now, nodeId, sequence);
	}

	public int nodeId() {
		return nodeId;
	}

	/**
	 * 시계가 {@code targetMillis}에 닿을 때까지 바쁘게 기다린다. 기다리는 시간이 최대 몇 ms라서
	 * 스레드를 재우는 것보다 싸다. 대신 기다리는 동안 CPU 코어 하나를 쓴다.
	 */
	private long waitUntil(long targetMillis) {
		long now = clock.getAsLong();
		while (now < targetMillis) {
			Thread.onSpinWait();
			now = clock.getAsLong();
		}
		return now;
	}
}
