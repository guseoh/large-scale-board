package io.github.guseoh.board.system.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 부하 기준선 측정용 API. (10절)
 *
 * <ul>
 *   <li>{@code /ping}: 앱과 네트워크만의 최대 처리량</li>
 *   <li>{@code /ping/db}: 커넥션 풀을 거쳐 DB를 한 번 다녀오는 요청의 최대 처리량</li>
 * </ul>
 * 두 값의 차이가 "DB 왕복 한 번의 비용"이고, 이후 모든 API 성능의 상한이 된다.
 *
 * <p>application 계층을 거치지 않고 {@link JdbcTemplate}을 직접 쓰는 이유: 측정 대상이 "커넥션 획득 + 쿼리 한 번"
 * 그 자체이기 때문이다. 서비스 계층이나 {@code @Transactional}을 끼우면 트랜잭션 시작·커밋 비용(autocommit 전환 등)이
 * 섞여 기준선이 흐려진다. 비즈니스 도메인에서는 이렇게 하지 않는다.
 */
@RestController
@RequestMapping("/api/v1/ping")
public class PingController {

	private final JdbcTemplate jdbcTemplate;

	public PingController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping
	public PingResponse ping() {
		return PingResponse.UP;
	}

	/** DB에 닿지 못하면 {@code CannotGetJdbcConnectionException}이 나고, 전역 핸들러가 503 DB_UNAVAILABLE로 바꾼다. */
	@GetMapping("/db")
	public PingResponse pingDb() {
		jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		return PingResponse.UP;
	}

	public record PingResponse(String status) {
		static final PingResponse UP = new PingResponse("UP");
	}
}
