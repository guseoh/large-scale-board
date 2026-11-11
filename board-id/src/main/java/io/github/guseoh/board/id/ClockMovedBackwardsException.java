package io.github.guseoh.board.id;

/**
 * 시계가 허용 범위보다 크게 뒤로 가서 ID를 만들 수 없을 때 던진다.
 *
 * <p>중복 ID를 내는 것보다 실패하는 편이 낫다. 중복 PK는 INSERT 실패로 끝나지만, 다른 테이블이 같은
 * ID를 참조하면 데이터가 조용히 섞인다.
 */
public class ClockMovedBackwardsException extends IllegalStateException {

	public ClockMovedBackwardsException(long lastMillis, long nowMillis) {
		super("시계가 " + (lastMillis - nowMillis) + "ms 뒤로 갔습니다. "
				+ "마지막 발급 시각=" + lastMillis + ", 현재 시각=" + nowMillis);
	}
}
