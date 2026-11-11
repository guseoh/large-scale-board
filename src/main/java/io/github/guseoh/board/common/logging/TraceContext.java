package io.github.guseoh.board.common.logging;

import org.slf4j.MDC;

/**
 * 요청 추적 ID의 이름과 조회 방법을 한곳에 모은다. (D0-07)
 *
 * <p>값을 넣고 지우는 것은 {@link RequestTraceFilter}만 한다. 나머지 코드는 읽기만 한다.
 */
public final class TraceContext {

	/** 요청·응답 헤더 이름. */
	public static final String HEADER = "X-Request-Id";

	/** MDC 키. 로그 패턴의 {@code %X{traceId}}와 ECS JSON 로그의 필드 이름이 된다. */
	public static final String MDC_KEY = "traceId";

	private TraceContext() {
	}

	/** 현재 스레드의 추적 ID. 필터를 거치지 않은 스레드(스케줄러, 별도 스레드 풀)에서는 null이다. */
	public static String currentTraceId() {
		return MDC.get(MDC_KEY);
	}
}
