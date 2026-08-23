package io.github.guseoh.board.board.member;

public record MemberSignUpResponse(
        Long memberId,
        String email,
        String nickname,
        String role
) {
    public static MemberSignUpResponse from(Member member) {
        return new MemberSignUpResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole().name()
        );
    }

}
