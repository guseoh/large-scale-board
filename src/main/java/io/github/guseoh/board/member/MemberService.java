package io.github.guseoh.board.member;

import io.github.guseoh.board.global.exception.ConflictException;
import io.github.guseoh.board.global.exception.NotFoundException;
import io.github.guseoh.board.member.domain.Member;
import io.github.guseoh.board.member.domain.MemberRepository;
import io.github.guseoh.board.member.web.MemberCreateRequest;
import io.github.guseoh.board.member.web.MemberResponse;
import io.github.guseoh.board.member.web.MemberUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository members;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        if (members.existsByUsername(request.username()) || members.existsByEmail(request.email())) {
            throw new ConflictException("Username or email already exists");
        }
        Member member = new Member(request.username(), request.email(), passwordEncoder.encode(request.password()));
        return MemberResponse.from(members.save(member));
    }

    public MemberResponse me(String username) {
        return MemberResponse.from(find(username));
    }

    @Transactional
    public MemberResponse update(String username, MemberUpdateRequest request) {
        Member member = find(username);
        if (!member.getEmail().equals(request.email()) && members.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }
        member.update(request.email());
        return MemberResponse.from(member);
    }

    @Transactional
    public void delete(String username) {
        members.delete(find(username));
    }

    public Member find(String username) {
        return members.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Member not found"));
    }
}
