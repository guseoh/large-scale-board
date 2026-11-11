package io.github.guseoh.board.common.error;

/**
 * 비즈니스 규칙 위반을 알리는 예외. 도메인 예외는 모두 이 예외에 도메인 {@link ErrorCode}를 담아
 * 던진다.
 *
 * <p>규칙 위반은 예상한 흐름이므로 스택 트레이스가 필요 없다. 스택을 채우지 않아 생성 비용을
 * 줄인다. 원인 예외를 감싸야 하는 경우에는 cause로 원인을 남긴다.
 */
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, errorCode.message(), null);
	}

	public BusinessException(ErrorCode errorCode, String detail) {
		this(errorCode, detail, null);
	}

	public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
		super(detail, cause, false, false);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
