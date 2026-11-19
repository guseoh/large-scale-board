package io.github.guseoh.board.txlab;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 실험용 빈. @Component를 쓰지 않고 테스트가 @Import할 때만 등록해서 다른 테스트에 영향이 없다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TxLabConfig {

	@Bean
	TxLabService txLabService(JdbcTemplate jdbcTemplate) {
		return new TxLabService(jdbcTemplate);
	}

	@Bean
	TxInner txInner(JdbcTemplate jdbcTemplate) {
		return new TxInner(jdbcTemplate);
	}

	@Bean
	TxOuter txOuter(TxInner txInner, JdbcTemplate jdbcTemplate) {
		return new TxOuter(txInner, jdbcTemplate);
	}
}
