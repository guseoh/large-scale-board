package io.github.guseoh.board.probe;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시각 타입별 저장 동작을 비교하는 탐침 엔티티. 1-A가 끝나면 지운다.
 */
@Entity
@Table(name = "time_probe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TimeProbe {

	@Id
	private Long id;

	private LocalDateTime localDt;

	private Instant instant;
}
