package io.github.guseoh.board.member.api;

import io.github.guseoh.board.member.domain.Member;
import io.github.guseoh.board.member.domain.Role;

import java.time.Instant;

public record MemberResponse(
        String id, String email, String nickname, Role role, Instant createdAt

) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                String.valueOf(member.getId()),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}

/*
    - ID가 String인 이유는 Snowflake ID가 2^53을 넘어 JavaScript에서 끝자리가 바뀌기 때문
    - passwordHash 필드가 없어서 응답에 해시가 노출될 수 없다
 */