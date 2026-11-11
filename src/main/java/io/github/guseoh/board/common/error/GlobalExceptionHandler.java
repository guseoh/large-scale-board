package io.github.guseoh.board.common.error;

import io.github.guseoh.board.common.logging.TraceContext;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 컨트롤러에서 나온 모든 예외를 RFC 9457 ProblemDetail + {@code code}, {@code traceId}, {@code errors} 형식으로 바꾼다. (D0-04)
 *
 * <p>{@link ResponseEntityExceptionHandler}를 상속하는 이유: Spring MVC가 던지는 기본 예외(검증 실패, 없는 경로,
 * 지원하지 않는 메서드, 읽을 수 없는 본문, 타입 불일치 등 약 20종)를 이미 ProblemDetail로 바꿔 준다.
 * 우리는 그 결과가 모두 지나가는 {@link #handleExceptionInternal}에서 공통 필드만 덧붙이면 된다.
 *
 * <p>한계: 서블릿 필터에서 난 예외(1단계의 인증 실패)는 DispatcherServlet 밖이라 이 핸들러를 거치지 않는다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// ---------------------------------------------------------------------
	// 우리가 정의한 예외
	// ---------------------------------------------------------------------

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		if (errorCode.status().is5xxServerError()) {
			log.error("[{}] {}", errorCode.code(), ex.getMessage(), ex);
			return toResponse(errorCode, errorCode.message()); // 5xx에는 내부 메시지를 내보내지 않는다
		}
		log.warn("[{}] {}", errorCode.code(), ex.getMessage());
		return toResponse(errorCode, ex.getMessage());
	}

	/**
	 * DB 커넥션을 얻지 못했거나 트랜잭션을 시작하지 못한 경우. 앱의 버그(500)와 의존 구성 요소의 장애(503)를
	 * 지표에서 구분하려고 따로 잡는다. (13절 장애 훈련)
	 *
	 * <ul>
	 *   <li>{@link DataAccessResourceFailureException}: JdbcTemplate 경로. 하위 클래스인
	 *       {@code CannotGetJdbcConnectionException}이 HikariCP의 커넥션 획득 실패를 감싼다.</li>
	 *   <li>{@link CannotCreateTransactionException}: {@code @Transactional} 경로. 트랜잭션 매니저가 커넥션을 얻지 못하면 이것을 던진다.</li>
	 * </ul>
	 */
	@ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
	public ResponseEntity<ProblemDetail> handleDatabaseUnavailable(Exception ex) {
		log.error("[{}] {}", CommonErrorCode.DB_UNAVAILABLE.code(), ex.getMessage(), ex);
		return toResponse(CommonErrorCode.DB_UNAVAILABLE, CommonErrorCode.DB_UNAVAILABLE.message());
	}

	/** 그 밖의 모든 예외. 원인은 로그로만 남기고 응답에는 고정 문구만 준다(SQL·클래스 이름 노출 방지). */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
		log.error("[{}] {}", CommonErrorCode.INTERNAL_ERROR.code(), ex.getMessage(), ex);
		return toResponse(CommonErrorCode.INTERNAL_ERROR, CommonErrorCode.INTERNAL_ERROR.message());
	}

	// ---------------------------------------------------------------------
	// Spring MVC 기본 예외: 부모 클래스가 만든 ProblemDetail에 공통 필드를 덧붙인다
	// ---------------------------------------------------------------------

	/** {@code @Valid @RequestBody} 검증 실패. 필드별 사유를 {@code errors}에 담는다. */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldErrorDetail> errors = new ArrayList<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.add(new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()));
		}
		for (ObjectError globalError : ex.getBindingResult().getGlobalErrors()) {
			errors.add(new FieldErrorDetail(globalError.getObjectName(), globalError.getDefaultMessage()));
		}
		ex.getBody().setProperty("errors", errors);
		return handleExceptionInternal(ex, ex.getBody(), headers, status, request);
	}

	/** {@code @RequestParam @Max(100)}처럼 메서드 파라미터에 건 제약의 검증 실패. */
	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(
			HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldErrorDetail> errors = new ArrayList<>();
		ex.getParameterValidationResults().forEach(result -> {
			String parameterName = result.getMethodParameter().getParameterName();
			result.getResolvableErrors().forEach(error ->
					errors.add(new FieldErrorDetail(parameterName, error.getDefaultMessage())));
		});
		ex.getBody().setProperty("errors", errors);
		return handleExceptionInternal(ex, ex.getBody(), headers, status, request);
	}

	/**
	 * Spring MVC 기본 예외의 응답이 모두 지나가는 지점. 여기서 {@code code}, {@code traceId}를 덧붙이고 로그 수준을 정한다.
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
		ErrorCode errorCode = CommonErrorCode.fromStatus(statusCode);
		logFrameworkException(ex, statusCode, errorCode);

		ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
		if (response != null && response.getBody() instanceof ProblemDetail problemDetail) {
			// Spring 기본 문구("Invalid request content." 등) 대신 우리 문구로 통일한다.
			problemDetail.setDetail(errorCode.message());
			decorate(problemDetail, errorCode);
		}
		return response;
	}

	// ---------------------------------------------------------------------
	// 내부 도우미
	// ---------------------------------------------------------------------

	private ResponseEntity<ProblemDetail> toResponse(ErrorCode errorCode, String detail) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.status(), detail);
		decorate(problemDetail, errorCode);
		return ResponseEntity.status(errorCode.status()).body(problemDetail);
	}

	/** 모든 오류 응답에 공통으로 붙는 필드. {@code instance}(요청 경로)는 Spring MVC가 응답을 쓸 때 채운다. */
	private void decorate(ProblemDetail problemDetail, ErrorCode errorCode) {
		problemDetail.setProperty("code", errorCode.code());
		problemDetail.setProperty("traceId", TraceContext.currentTraceId());
	}

	/** 4xx는 WARN(스택 없음), 404는 기록하지 않음, 5xx는 ERROR(스택 포함). (6절 예외 매핑) */
	private void logFrameworkException(Exception ex, HttpStatusCode statusCode, ErrorCode errorCode) {
		if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
			return; // 스캐너·오타 요청이 로그를 채우지 않게 한다
		}
		if (statusCode.is5xxServerError()) {
			log.error("[{}] {}", errorCode.code(), ex.getMessage(), ex);
			return;
		}
		log.warn("[{}] {}", errorCode.code(), ex.getMessage());
	}
}
