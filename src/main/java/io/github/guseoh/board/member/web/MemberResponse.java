package io.github.guseoh.board.member.web;
import io.github.guseoh.board.member.domain.Member;
import java.time.LocalDateTime;
public record MemberResponse(Long id, String username, String email, LocalDateTime createdAt) {
    public static MemberResponse from(Member member) { return new MemberResponse(member.getId(), member.getUsername(), member.getEmail(), member.getCreatedAt()); }
}
