package io.github.guseoh.datagen.h01;

import io.github.guseoh.datagen.table.ExpLoadArticleRow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import org.springframework.data.domain.Persistable;

/**
 * 방식 A, B: ID를 애플리케이션이 넣고, {@link Persistable}로 "새 엔티티"임을 Spring Data에 알린다.
 * (D0-12)
 *
 * <p>왜 필요한가: Spring Data JPA의 {@code save()}는 ID가 null이면 새 엔티티로 보고 {@code
 * persist}를, 아니면 기존 엔티티로 보고 {@code merge}를 한다. ID를 미리 넣으면 항상 {@code merge}가
 * 되고, {@code merge}는 그 ID의 행이 있는지 확인하려고 INSERT 전에 SELECT를 한 번 더 보낸다(방식
 * A'). {@code isNew()}를 직접 구현하면 이 SELECT가 사라진다.
 *
 * <p>1단계에서 만들 엔티티 공통 상위 클래스가 이 방식을 따른다.
 */
@Entity
@Table(name = "exp_load_article")
public class ExpLoadArticleEntity implements Persistable<Long> {

	@Id
	private Long id;

	private long boardId;
	private long writerId;

	@Column(length = 100)
	private String title;

	private String content;
	private LocalDateTime createdAt;

	@Transient
	private boolean isNew = true;

	protected ExpLoadArticleEntity() {
	}

	public static ExpLoadArticleEntity from(ExpLoadArticleRow row) {
		ExpLoadArticleEntity entity = new ExpLoadArticleEntity();
		entity.id = row.id();
		entity.boardId = row.boardId();
		entity.writerId = row.writerId();
		entity.title = row.title();
		entity.content = row.content();
		entity.createdAt = row.createdAt();
		return entity;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	/** 저장했거나 DB에서 읽은 뒤에는 새 엔티티가 아니다. */
	@PostPersist
	@PostLoad
	void markNotNew() {
		this.isNew = false;
	}
}
