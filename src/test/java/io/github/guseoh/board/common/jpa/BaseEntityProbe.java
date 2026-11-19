package io.github.guseoh.board.common.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * BaseEntity 계약을 검증하기 위한 탐침 엔티티. ID는 직접 할당한다(Snowflake와 같은 상황).
 */
@Entity
@Table(name = "base_entity_probe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BaseEntityProbe extends BaseEntity {

	@Id
	private Long id;

	private String name;

	public void rename(String name) {
		this.name = name;
	}
}
