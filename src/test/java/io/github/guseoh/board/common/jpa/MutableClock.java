package io.github.guseoh.board.common.jpa;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 테스트에서 시각을 원하는 값으로 옮길 수 있는 Clock. Clock.fixed는 값을 바꿀 수 없어서 따로 둔다.
 */
public class MutableClock extends Clock {

	private final AtomicReference<Instant> now;

	public MutableClock(Instant start) {
		this.now = new AtomicReference<>(start);
	}

	public void set(Instant instant) {
		now.set(instant);
	}

	public void advance(Duration duration) {
		now.updateAndGet(current -> current.plus(duration));
	}

	@Override
	public ZoneId getZone() {
		return ZoneOffset.UTC;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return this;
	}

	@Override
	public Instant instant() {
		return now.get();
	}
}
