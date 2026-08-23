package io.github.guseoh.board.board.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;


@Getter
@Entity
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_members_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_members_nickname",
                        columnNames = "nickname"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Member(
            String email,
            String nickname,
            String passwordHash,
            MemberRole role
    ) {
        this.email = normalizeEmail(email);
        this.nickname = normalizeNickname(nickname);
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static Member createUser(
            String email,
            String nickname,
            String encodedPassword
    ) {
        return new Member(
                email,
                nickname,
                encodedPassword,
                MemberRole.USER
        );
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    private static String normalizeNickname(String nickname) {
        return nickname.strip();
    }

    private static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}

/*
    Member의 책임
    - 회원의 영속 상태 표현
    - 이메일과 닉네임 정규화
    - 신규 일반 회원 생성
    - 생성·수정 시각 관리
    - 기본 권한을 USER로 제한
 */