package io.github.guseoh.datagen.h01;

import io.github.guseoh.datagen.table.ExpLoadArticleRow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 방식 B': PK를 DB의 AUTO_INCREMENT로 만든다({@code IDENTITY} 전략). 나머지는 방식 B와 같다.
 *
 * <p>확인하려는 것: Hibernate는 IDENTITY 전략에서 JDBC 배치 INSERT를 끄는가. INSERT를 실행해야 ID를
 * 알 수 있으므로, 영속화하는 순간 INSERT를 하나씩 바로 보낼 수밖에 없다.
 */
@Entity
@Table(name = "exp_load_article_identity")
public class ExpLoadArticleIdentityEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private long boardId;
	private long writerId;

	@Column(length = 100)
	private String title;

	private String content;
	private LocalDateTime createdAt;

	protected ExpLoadArticleIdentityEntity() {
	}

	/** 생성기가 만든 Snowflake ID는 버리고 DB가 ID를 정한다. */
	public static ExpLoadArticleIdentityEntity from(ExpLoadArticleRow row) {
		ExpLoadArticleIdentityEntity entity = new ExpLoadArticleIdentityEntity();
		entity.boardId = row.boardId();
		entity.writerId = row.writerId();
		entity.title = row.title();
		entity.content = row.content();
		entity.createdAt = row.createdAt();
		return entity;
	}
}
