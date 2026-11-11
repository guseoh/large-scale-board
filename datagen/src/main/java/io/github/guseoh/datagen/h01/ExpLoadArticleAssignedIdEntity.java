package io.github.guseoh.datagen.h01;

import io.github.guseoh.datagen.table.ExpLoadArticleRow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 방식 A': {@link ExpLoadArticleEntity}와 같은 테이블이지만 {@code Persistable}을 구현하지 않는다.
 *
 * <p>확인하려는 것: ID를 직접 넣으면 Spring Data가 {@code merge}를 고르고, INSERT마다 SELECT가
 * 하나씩 더 나가는가.
 */
@Entity
@Table(name = "exp_load_article")
public class ExpLoadArticleAssignedIdEntity {

	@Id
	private Long id;

	private long boardId;
	private long writerId;

	@Column(length = 100)
	private String title;

	private String content;
	private LocalDateTime createdAt;

	protected ExpLoadArticleAssignedIdEntity() {
	}

	public static ExpLoadArticleAssignedIdEntity from(ExpLoadArticleRow row) {
		ExpLoadArticleAssignedIdEntity entity = new ExpLoadArticleAssignedIdEntity();
		entity.id = row.id();
		entity.boardId = row.boardId();
		entity.writerId = row.writerId();
		entity.title = row.title();
		entity.content = row.content();
		entity.createdAt = row.createdAt();
		return entity;
	}
}
