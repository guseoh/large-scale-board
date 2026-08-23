package io.github.guseoh.board.board.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberSignUpResponse signUp(MemberSignUpRequest request) {

        String email = normalizeEmail(request.email());
        String nickname = request.nickname().strip();

        validateDuplicate(email, nickname);

        String encodedPassword =
                passwordEncoder.encode(request.password());

        Member member = Member.createUser(
                email,
                nickname,
                encodedPassword
        );
    }
}
