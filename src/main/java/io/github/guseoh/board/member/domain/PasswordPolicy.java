package io.github.guseoh.board.member.domain;

import io.github.guseoh.board.common.error.BusinessException;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 정책. 8자 이상, UTF-8 72바이트 이하.
 *
 * <p>72바이트 제한은 BCrypt 알고리즘의 한계다. 그보다 긴 입력은 뒷부분이 해시에 반영되지 않거나
 * 인코더가 예외를 던진다. 한글은 한 글자가 3바이트라서 글자 수가 아니라 바이트로 검사한다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PasswordPolicy {

	static final int MIN_LENGTH = 8;
	static final int MAX_BYTES = 72;

	public static void validate(String rawPassword) {
		if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
			throw new BusinessException(
					MemberErrorCode.MEMBER_INVALID_PASSWORD_POLICY,
					"비밀번호는 " + MIN_LENGTH + "자 이상이어야 합니다.");
		}
		if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
			throw new BusinessException(
					MemberErrorCode.MEMBER_INVALID_PASSWORD_POLICY,
					"비밀번호는 UTF-8 기준 " + MAX_BYTES + "바이트 이하여야 합니다.");
		}
	}
}
