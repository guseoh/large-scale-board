package io.github.guseoh.board.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 추적 필터의 단위 테스트. 서블릿 컨테이너 없이 Spring의 Mock 요청·응답으로 필터만 실행한다.
 */
class RequestTraceFilterTest {

	private final RequestTraceFilter filter = new RequestTraceFilter(true);

	@AfterEach
	void clearMdc() {
		MDC.clear();
	}

	@Test
	void 형식에_맞는_요청_헤더는_그대로_쓰고_응답_헤더로_돌려준다() throws Exception {
		String incoming = "client-abc-12345678";

		Result result = run(incoming);

		assertThat(result.traceIdSeenByChain()).isEqualTo(incoming);
		assertThat(result.response().getHeader(TraceContext.HEADER)).isEqualTo(incoming);
	}

	@Test
	void 헤더가_없으면_32자리_16진수를_새로_만든다() throws Exception {
		Result result = run(null);

		assertThat(result.traceIdSeenByChain()).matches("^[0-9a-f]{32}$");
		assertThat(result.response().getHeader(TraceContext.HEADER))
				.isEqualTo(result.traceIdSeenByChain());
	}

	@Test
	void 형식에_맞지_않는_헤더는_버리고_새로_만든다() throws Exception {
		// 개행을 넣어 가짜 로그 줄을 만드는 시도, 너무 짧은 값, 너무 긴 값
		String[] forgedIds = {"abc\nFAKE LOG LINE", "short", "x".repeat(65), "has space 1234"};
		for (String forged : forgedIds) {
			Result result = run(forged);

			assertThat(result.traceIdSeenByChain()).isNotEqualTo(forged).matches("^[0-9a-f]{32}$");
		}
	}

	@Test
	void 요청이_끝나면_MDC를_지운다() throws Exception {
		run("client-abc-12345678");

		assertThat(MDC.get(TraceContext.MDC_KEY)).isNull();
	}

	@Test
	void 체인에서_예외가_나도_MDC를_지운다() {
		MockFilterChain failingChain = new MockFilterChain() {
			@Override
			public void doFilter(
					jakarta.servlet.ServletRequest request,
					jakarta.servlet.ServletResponse response)
					throws IOException {
				throw new IOException("client aborted");
			}
		};

		try {
			filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/ping"),
					new MockHttpServletResponse(), failingChain);
		} catch (Exception ignored) {
			// 예외 전파 여부가 아니라 MDC 정리 여부를 본다
		}

		assertThat(MDC.get(TraceContext.MDC_KEY)).isNull();
	}

	private Result run(String incomingHeader) throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/ping");
		if (incomingHeader != null) {
			request.addHeader(TraceContext.HEADER, incomingHeader);
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> seen = new AtomicReference<>();
		// 체인 안(= 컨트롤러가 실행되는 시점)에서 MDC에 어떤 값이 보이는지 기록한다.
		MockFilterChain chain = new MockFilterChain() {
			@Override
			public void doFilter(
					jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
				seen.set(MDC.get(TraceContext.MDC_KEY));
			}
		};
		filter.doFilter(request, response, chain);
		return new Result(seen.get(), response);
	}

	private record Result(String traceIdSeenByChain, MockHttpServletResponse response) {
	}
}
