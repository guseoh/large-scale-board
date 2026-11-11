package io.github.guseoh.board.common.error;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guseoh.board.common.logging.LoggingConfig;
import io.github.guseoh.board.common.logging.TraceContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 오류 응답 형식(6절)을 MockMvc로 검증한다. 예외를 일부러 던지는 테스트 전용 컨트롤러를 쓴다.
 *
 * <p>테스트 클래스 안의 정적 중첩 컨트롤러는 Spring Boot의 TestTypeExcludeFilter가 컴포넌트 스캔에서 빼므로,
 * 다른 테스트의 컨텍스트에 섞이지 않는다. 여기서만 {@code @Import}로 등록한다.
 */
@WebMvcTest(controllers = GlobalExceptionHandlerTest.FailingController.class)
@Import({GlobalExceptionHandlerTest.FailingController.class, LoggingConfig.class})
class GlobalExceptionHandlerTest {

	private static final String TRACE_ID = "test-trace-0001";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void 본문_검증_실패는_400_INVALID_INPUT과_필드별_사유를_준다() throws Exception {
		mockMvc.perform(post("/test/validate")
						.header(TraceContext.HEADER, TRACE_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\": \"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.traceId").value(TRACE_ID))
				.andExpect(jsonPath("$.instance").value("/test/validate"))
				.andExpect(jsonPath("$.errors[0].field").value("title"))
				.andExpect(jsonPath("$.errors[0].reason").isNotEmpty());
	}

	@Test
	void 파라미터_제약_위반도_400과_errors를_준다() throws Exception {
		mockMvc.perform(get("/test/param").param("size", "1000"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.errors[0].field").value("size"));
	}

	@Test
	void 읽을_수_없는_본문은_400_INVALID_INPUT이다() throws Exception {
		mockMvc.perform(post("/test/validate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{not json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	void 타입_불일치는_400_INVALID_INPUT이다() throws Exception {
		mockMvc.perform(get("/test/param").param("size", "abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	void 없는_경로는_404_NOT_FOUND다() throws Exception {
		mockMvc.perform(get("/test/does-not-exist"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"))
				.andExpect(jsonPath("$.traceId").isNotEmpty());
	}

	@Test
	void 지원하지_않는_메서드는_405_METHOD_NOT_ALLOWED다() throws Exception {
		mockMvc.perform(post("/test/boom"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
	}

	@Test
	void BusinessException은_코드에_정의된_상태와_코드를_쓴다() throws Exception {
		mockMvc.perform(get("/test/business"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("TEST_DUPLICATED"))
				.andExpect(jsonPath("$.detail").value("이미 존재합니다: 42"));
	}

	@Test
	void 예상하지_못한_예외는_500이고_내부_메시지를_노출하지_않는다() throws Exception {
		mockMvc.perform(get("/test/boom"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
				.andExpect(jsonPath("$.detail").value(CommonErrorCode.INTERNAL_ERROR.message()))
				.andExpect(content().string(not(containsString("SELECT secret"))));
	}

	@Test
	void DB_커넥션_획득_실패는_503_DB_UNAVAILABLE이다() throws Exception {
		mockMvc.perform(get("/test/db-down"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.code").value("DB_UNAVAILABLE"))
				.andExpect(content().string(not(containsString("Connection is not available"))))
				.andExpect(header().exists(TraceContext.HEADER));
	}

	// ------------------------------------------------------------------

	record CreateRequest(@NotBlank @Size(max = 100) String title) {
	}

	enum TestErrorCode implements ErrorCode {
		TEST_DUPLICATED;

		@Override
		public HttpStatus status() {
			return HttpStatus.CONFLICT;
		}

		@Override
		public String code() {
			return name();
		}

		@Override
		public String message() {
			return "이미 존재합니다.";
		}
	}

	@RestController
	static class FailingController {

		@PostMapping("/test/validate")
		void validate(@Valid @RequestBody CreateRequest request) {
		}

		@GetMapping("/test/param")
		void param(@RequestParam @Max(100) int size) {
		}

		@GetMapping("/test/business")
		void business() {
			throw new BusinessException(TestErrorCode.TEST_DUPLICATED, "이미 존재합니다: 42");
		}

		@GetMapping("/test/boom")
		void boom() {
			throw new IllegalStateException("SELECT secret FROM internal_table");
		}

		@GetMapping("/test/db-down")
		void dbDown() {
			throw new CannotGetJdbcConnectionException("HikariPool-1 - Connection is not available, request timed out after 30000ms");
		}
	}
}
