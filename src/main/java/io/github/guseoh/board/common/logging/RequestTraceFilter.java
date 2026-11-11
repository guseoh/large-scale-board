package io.github.guseoh.board.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 요청마다 추적 ID를 정해 MDC와 응답 헤더에 넣고, 요청이 끝나면 접근 로그 한 줄을 남긴다. (D0-07)
 *
 * <p>가장 먼저 실행되도록 {@link LoggingConfig}에서 최우선 순서로 등록한다. 그래야 뒤의 필터, 인터셉터,
 * 컨트롤러, 예외 핸들러가 남기는 모든 로그에 같은 traceId가 붙는다.
 *
 * <p>{@code @Component}를 붙이지 않는 이유: 붙이면 Spring Boot가 필터를 자동 등록하고, 설정에서 순서를 정하려고
 * 다시 등록하면 두 번 실행된다. 등록은 {@code FilterRegistrationBean} 한 곳에서만 한다.
 */
public class RequestTraceFilter extends OncePerRequestFilter {

	private static final Logger accessLog = LoggerFactory.getLogger("ACCESS");

	/** 외부에서 받은 값은 로그에 그대로 찍히므로 형식을 제한한다(개행·제어 문자를 넣은 로그 위조 방지). */
	private static final Pattern VALID_TRACE_ID = Pattern.compile("^[A-Za-z0-9-]{8,64}$");

	private static final HexFormat HEX = HexFormat.of();

	private final boolean accessLogEnabled;

	public RequestTraceFilter(boolean accessLogEnabled) {
		this.accessLogEnabled = accessLogEnabled;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String traceId = resolveTraceId(request.getHeader(TraceContext.HEADER));
		MDC.put(TraceContext.MDC_KEY, traceId);
		// 본문을 쓰기 시작하면(커밋되면) 헤더를 바꿀 수 없으므로 체인을 타기 전에 넣는다.
		response.setHeader(TraceContext.HEADER, traceId);

		long startNanos = System.nanoTime();
		try {
			chain.doFilter(request, response);
		} finally {
			try {
				writeAccessLog(request, response, startNanos);
			} finally {
				// Tomcat은 스레드를 재사용한다. 지우지 않으면 이 스레드가 받는 다음 요청에 이전 traceId가 남는다.
				MDC.remove(TraceContext.MDC_KEY);
			}
		}
	}

	static String resolveTraceId(String incoming) {
		if (incoming != null && VALID_TRACE_ID.matcher(incoming).matches()) {
			return incoming;
		}
		return newTraceId();
	}

	/**
	 * 32자리 16진수(128비트). W3C Trace Context의 trace-id와 같은 길이라 나중에 분산 트레이싱으로 바꿔도 형식이 유지된다.
	 *
	 * <p>{@code UUID.randomUUID()} 대신 {@link ThreadLocalRandom}을 쓰는 이유: 추적 ID는 보안 값이 아니므로
	 * SecureRandom이 필요 없고, 스레드별 난수 생성기라 요청이 몰려도 경합이 없다.
	 */
	static String newTraceId() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return HEX.toHexDigits(random.nextLong()) + HEX.toHexDigits(random.nextLong());
	}

	private void writeAccessLog(HttpServletRequest request, HttpServletResponse response, long startNanos) {
		if (!accessLogEnabled || request.getRequestURI().startsWith("/actuator")) {
			return;
		}
		long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
		// 쿼리 문자열은 남기지 않는다. 검색어나 토큰 같은 값이 로그에 남을 수 있다.
		// clientIp는 프록시가 없는 0~3단계 기준이다. 프록시를 두는 단계에서 X-Forwarded-For 신뢰 범위를 정한다.
		accessLog.info("method={} path={} status={} durationMs={} clientIp={}",
				request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs,
				request.getRemoteAddr());
	}
}
