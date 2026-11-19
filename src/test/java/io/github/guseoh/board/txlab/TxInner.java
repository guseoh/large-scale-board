package io.github.guseoh.board.txlab;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TxInner {

	private final JdbcTemplate jdbcTemplate;

	/** 기본 전파(REQUIRED): 바깥 트랜잭션에 참여한다. */
	@Transactional
	public void failInRequired() {
		jdbcTemplate.update("insert into tx_lab (note) values (?)", "inner-required");
		throw new IllegalStateException("안쪽 실패");
	}

	/** REQUIRES_NEW: 바깥과 별개의 새 트랜잭션을 연다. */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void failInRequiresNew() {
		jdbcTemplate.update("insert into tx_lab (note) values (?)", "inner-requires-new");
		throw new IllegalStateException("안쪽 실패");
	}
}
