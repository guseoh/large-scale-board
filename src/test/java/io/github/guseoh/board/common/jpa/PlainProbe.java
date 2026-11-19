package io.github.guseoh.board.common.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비교용 탐침: ID를 직접 할당하지만 Persistable도 BaseEntity도 쓰지 않는 평범한 엔티티.
 */
@Entity
@Table(name = "plain_probe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PlainProbe {

	@Id
	private Long id;

	private String name;
}
