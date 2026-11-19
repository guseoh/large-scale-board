package io.github.guseoh.board.txlab;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TxOuter {

	private final TxInner inner;
	private final JdbcTemplate jdbcTemplate;

	/** 안쪽 예외를 삼키면 바깥은 성공한 줄 안다. 하지만 트랜잭션은 이미 rollback-only로 표시됐다. */
	@Transactional
	public void swallowFailureOfRequired() {
		jdbcTemplate.update("insert into tx_lab (note) values (?)", "outer");
		try {
			inner.failInRequired();
		} catch (IllegalStateException ignored) {
			// 삼킨다
		}
	}

	@Transactional
	public void swallowFailureOfRequiresNew() {
		jdbcTemplate.update("insert into tx_lab (note) values (?)", "outer");
		try {
			inner.failInRequiresNew();
		} catch (IllegalStateException ignored) {
			// 삼킨다
		}
	}
}
