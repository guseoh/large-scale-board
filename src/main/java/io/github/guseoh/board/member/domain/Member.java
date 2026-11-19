package io.github.guseoh.board.member.domain;

import io.github.guseoh.board.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.boot.models.annotations.internal.EnumeratedJpaAnnotation;
import org.hibernate.type.SqlTypes;

import java.util.Locale;

@Entity
@Table(  // todo
        name = "member",
        uniqueConstraints =
        @UniqueConstraint(name = "uk_member_email", columnNames = "email")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // todo
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // todo
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String nickname;

    // Hibernate는 STRING 열거형을 MySQL ENUM 컬럼으로 만든다. 값을 추가할 때마다 ALTER가 필요해서 VARCHAR로 고정한다.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Role role;

    public static Member register(
            @NonNull Long id,
            @NonNull String email,
            @NonNull String passwordHash,
            @NonNull String nickname
    ) {
        return new Member(id, normalizeEmail(email), passwordHash, nickname.strip(), Role.USER);
    }

    private static String normalizeEmail(@NonNull String email) {
        return email.strip().toLowerCase(Locale.ROOT); // 공백 제거, 소문자
    }

}

/*
    - ID는 @Id만 있고 @GeneratedValue가 없습니다. 서비스가 Snowflake로 만든 값을 저장 전에 넘기고, Persistable 덕분에 INSERT만 나갑니다.
    - 생성자는 private이고 register()만 공개합니다. 이메일 정규화(공백 제거, 소문자)와 기본 권한을 엔티티 안에서 강제합니다. 비즈니스 규칙을 서비스가 아닌 엔티티에 두는 경량 DDD 원칙입니다.
    - @JdbcTypeCode(SqlTypes.VARCHAR)는 ENUM 컬럼 생성을 막습니다. 이 애너테이션을 빼면 직접 쓴 DDL과 달라져서 MemberSchemaTest가 실패하는지 직접 확인해 보셔도 좋습니다.


 */