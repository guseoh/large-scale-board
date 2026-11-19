package io.github.guseoh.board.txlab;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 트랜잭션 실험용 서비스. 일부러 "잘못 쓴" 코드를 담고 있다. 운영 코드가 아니다.
 */
@RequiredArgsConstructor
public class TxLabService {

	private final JdbcTemplate jdbcTemplate;

	@Transactional
	public boolean transactionalMethod() {
		return TransactionSynchronizationManager.isActualTransactionActive();
	}

	/** 트랜잭션 없는 진입점이 같은 클래스의 @Transactional 메서드를 this로 호출한다. */
	public boolean callTransactionalMethodOnThis() {
		return this.transactionalMethod();
	}

	public boolean callPrivateTransactionalMethod() {
		return privateTransactionalMethod();
	}

	@Transactional
	private boolean privateTransactionalMethod() {
		return TransactionSynchronizationManager.isActualTransactionActive();
	}

	@Transactional
	public void insertThenThrowChecked() throws IOException {
		insert("checked");
		throw new IOException("체크 예외");
	}

	@Transactional(rollbackFor = Exception.class)
	public void insertThenThrowCheckedWithRollbackFor() throws IOException {
		insert("checked-rollbackFor");
		throw new IOException("체크 예외");
	}

	@Transactional
	public void insertThenThrowRuntime() {
		insert("runtime");
		throw new IllegalStateException("런타임 예외");
	}

	public void insert(String note) {
		jdbcTemplate.update("insert into tx_lab (note) values (?)", note);
	}
}
