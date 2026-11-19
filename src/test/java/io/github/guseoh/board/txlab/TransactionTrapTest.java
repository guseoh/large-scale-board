package io.github.guseoh.board.txlab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guseoh.board.TestcontainersConfiguration;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * 1-B-3 트랜잭션 실험. @Transactional이 "붙어 있는데 안 먹는" 대표적인 경우와, 롤백 규칙을 테스트로 고정한다.
 * 모든 테스트는 현재 스프링의 실제 동작을 기록한다.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TxLabConfig.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class TransactionTrapTest {

	private final TxLabService labService;
	private final TxOuter outer;
	private final JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute(
				"create table if not exists tx_lab (id bigint auto_increment primary key, "
						+ "note varchar(50) not null)");
		jdbcTemplate.update("delete from tx_lab");
	}

	// ----- 프록시: @Transactional은 프록시를 거칠 때만 동작한다 -----

	@Test
	@DisplayName("밖에서 프록시를 통해 호출하면 트랜잭션이 열린다")
	void callThroughProxyOpensTransaction() {
		assertThat(labService.transactionalMethod()).isTrue();
	}

	@Test
	@DisplayName("함정: 같은 클래스 안에서 this로 호출하면 프록시를 건너뛰어 트랜잭션이 열리지 않는다")
	void selfInvocationBypassesProxy() {
		assertThat(labService.callTransactionalMethodOnThis()).isFalse();
	}

	@Test
	@DisplayName("함정: private 메서드의 @Transactional은 조용히 무시된다")
	void privateMethodIsIgnored() {
		assertThat(labService.callPrivateTransactionalMethod()).isFalse();
	}

	// ----- 롤백 규칙: 기본은 런타임 예외만 롤백한다 -----

	@Test
	@DisplayName("런타임 예외가 나면 롤백된다")
	void runtimeExceptionRollsBack() {
		assertThatThrownBy(labService::insertThenThrowRuntime)
				.isInstanceOf(IllegalStateException.class);

		assertThat(notes()).isEmpty();
	}

	@Test
	@DisplayName("함정: 체크 예외는 롤백되지 않고 커밋된다")
	void checkedExceptionCommitsByDefault() {
		assertThatThrownBy(labService::insertThenThrowChecked).isInstanceOf(IOException.class);

		assertThat(notes()).containsExactly("checked");
	}

	@Test
	@DisplayName("rollbackFor = Exception.class를 주면 체크 예외도 롤백된다")
	void rollbackForMakesCheckedExceptionRollBack() {
		assertThatThrownBy(labService::insertThenThrowCheckedWithRollbackFor)
				.isInstanceOf(IOException.class);

		assertThat(notes()).isEmpty();
	}

	// ----- 예외를 삼켜도 트랜잭션은 이미 망가졌다 -----

	@Test
	@DisplayName("함정: 안쪽(REQUIRED)의 예외를 삼켜도 바깥 커밋이 UnexpectedRollbackException으로 실패한다")
	void swallowedExceptionInRequiredMarksRollbackOnly() {
		assertThatThrownBy(outer::swallowFailureOfRequired)
				.isInstanceOf(UnexpectedRollbackException.class);

		assertThat(notes()).isEmpty(); // 바깥이 넣은 행까지 사라진다
	}

	@Test
	@DisplayName("안쪽이 REQUIRES_NEW면 안쪽만 롤백되고 바깥은 정상 커밋된다")
	void requiresNewIsolatesInnerFailure() {
		outer.swallowFailureOfRequiresNew();

		assertThat(notes()).containsExactly("outer");
	}

	private List<String> notes() {
		return jdbcTemplate.queryForList("select note from tx_lab order by id", String.class);
	}
}
