package io.github.guseoh.board.member.domain;

import io.github.guseoh.board.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

	MEMBER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
	MEMBER_INVALID_PASSWORD_POLICY(HttpStatus.BAD_REQUEST, "비밀번호가 정책에 맞지 않습니다.");

	private final HttpStatus status;
	private final String message;

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
