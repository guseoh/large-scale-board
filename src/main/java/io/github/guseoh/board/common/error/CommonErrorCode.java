package io.github.guseoh.board.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * 도메인에 속하지 않는 공통 오류 코드.
 */
public enum CommonErrorCode implements ErrorCode {

	INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 자원을 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
	CONFLICT(HttpStatus.CONFLICT, "요청이 현재 자원 상태와 충돌합니다."),
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	DB_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "데이터베이스를 일시적으로 사용할 수 없습니다.");

	private final HttpStatus status;
	private final String message;

	CommonErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	/**
	 * Spring MVC가 정한 상태 코드에 대응하는 공통 코드를 찾는다.
	 *
	 * <p>목록에 없는 상태(406, 413, 415 등)는 4xx면 {@link #INVALID_INPUT}, 그 밖은 {@link #INTERNAL_ERROR}로 묶는다.
	 * 이때 HTTP 상태는 Spring이 정한 값을 그대로 쓰고 {@code code}만 묶인다.
	 */
	public static CommonErrorCode fromStatus(HttpStatusCode statusCode) {
		for (CommonErrorCode errorCode : values()) {
			if (errorCode.status.value() == statusCode.value()) {
				return errorCode;
			}
		}
		return statusCode.is4xxClientError() ? INVALID_INPUT : INTERNAL_ERROR;
	}

	@Override
	public HttpStatus status() {
		return status;
	}

	@Override
	public String code() {
		return name();
	}

	@Override
	public String message() {
		return message;
	}
}
