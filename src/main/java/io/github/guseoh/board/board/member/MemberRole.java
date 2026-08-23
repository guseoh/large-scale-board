package io.github.guseoh.board.board.member;

public enum MemberRole {
    USER, ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
