package io.github.guseoh.board.member.application;

import io.github.guseoh.board.common.error.BusinessException;
import io.github.guseoh.board.id.SnowflakeIdGenerator;
import io.github.guseoh.board.member.domain.Member;
import io.github.guseoh.board.member.domain.MemberErrorCode;
import io.github.guseoh.board.member.domain.MemberRepository;
import io.github.guseoh.board.member.domain.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSignUpService {

	private static final String EMAIL_UNIQUE_KEY = "uk_member_email";

	private final MemberRepository memberRepository;
	private final SnowflakeIdGenerator idGenerator;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Member signUp(String email, String rawPassword, String nickname) {
		PasswordPolicy.validate(rawPassword);

		Member member = Member.register(
				idGenerator.nextId(), email, passwordEncoder.encode(rawPassword), nickname);
		try {
			// save()만 쓰면 INSERT가 커밋 시점으로 미뤄져 아래 catch가 예외를 잡지 못한다. 즉시 내보낸다.
			return memberRepository.saveAndFlush(member);
		} catch (DataIntegrityViolationException e) {
			if (isEmailDuplicate(e)) {
				throw new BusinessException(MemberErrorCode.MEMBER_EMAIL_DUPLICATED);
			}
			throw e;
		}
	}

	private static boolean isEmailDuplicate(DataIntegrityViolationException e) {
		String message = e.getMostSpecificCause().getMessage();
		return message != null && message.contains(EMAIL_UNIQUE_KEY);
	}
}
