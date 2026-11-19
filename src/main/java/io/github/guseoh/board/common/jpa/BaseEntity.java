package io.github.guseoh.board.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity implements Persistable<Long> {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    @LastModifiedDate
    @Column(nullable = false)
    private Instant modifiedAt;

    // DB에 저장하지 않는 플래그
    @Transient
    @Getter(AccessLevel.NONE)
    private boolean newEntity = true;

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostPersist // 저장 직후
    @PostLoad    // 조회 직후
    void markNotNew() {
        this.newEntity = false;
    }

}


/*
     1. BaseEntity의 "Persistable을 구현해야 하는 이유?"
        문제: Spring Data의 save()는 "새 엔티티인가?"를 판단해서 새것이면 persist(INSERT만), 아니면
             merge(SELECT 후 INSERT/UPDATE)를 실행한다.
        상황: ID를 Snowflake로 미리 만들어 넣으니 ID가 항상 non-null입니다.
             그래서 새 회원도 "기존 엔티티"로 취급되어 merge를 타고 SELECT가 한 번 더 나갑니다.
        해결: Persistable.isNew()를 구현하면 판단 기준을 우리가 직접 정할 수 있다.
 */