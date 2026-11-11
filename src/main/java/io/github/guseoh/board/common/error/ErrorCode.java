package io.github.guseoh.board.common.error;

import org.springframework.http.HttpStatus;

/**
 * 오류 코드의 계약. (D0-06)
 *
 * <p>공통 코드는 {@link CommonErrorCode}가, 도메인 코드는 각 도메인의 enum(예: {@code ArticleErrorCode})이 구현한다.
 * 하나의 전역 enum에 모으면 모든 도메인이 같은 파일을 고치게 되고 서비스 분리 때 걸림돌이 된다.
 *
 * <p>{@link #code()}는 클라이언트와 테스트가 분기하는 기준이다. 문구({@link #message()})는 바뀔 수 있으므로
 * 문구로 분기하지 않는다. 도메인 코드 이름은 {@code 도메인_사유}(예: {@code ARTICLE_NOT_FOUND}) 형태로 짓는다.
 */
public interface ErrorCode {

	HttpStatus status();

	String code();

	String message();
}
