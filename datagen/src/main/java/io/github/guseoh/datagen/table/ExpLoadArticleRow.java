package io.github.guseoh.datagen.table;

import java.time.LocalDateTime;

/**
 * H-01 실험 테이블 {@code exp_load_article}의 행 하나. 게시글과 비슷한 모양이다. (Step-0-Design
 * 12절)
 *
 * @param createdAt UTC 기준 시각. DB 컬럼은 {@code DATETIME(3)}이고 시간대를 저장하지 않는다.
 */
public record ExpLoadArticleRow(
		long id,
		long boardId,
		long writerId,
		String title,
		String content,
		LocalDateTime createdAt) {
}
