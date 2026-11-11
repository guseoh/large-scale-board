package io.github.guseoh.datagen;

import io.github.guseoh.datagen.h01.LoadMethod;
import java.nio.file.Path;
import java.time.Instant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * {@code datagen.*} 설정. 명령줄에서 {@code --datagen.rows=100000}처럼 덮어쓴다.
 *
 * @param job          {@code csv}(CSV만 만든다) 또는 {@code h01}(적재 방식 하나를 실행한다)
 * @param seed         전체 시드. 같은 시드와 같은 행 수는 같은 데이터를 만든다
 * @param rows         규모 1.0일 때의 행 수
 * @param scale        전체 규모 비율. 0.1이면 10분의 1로 줄인다
 * @param baseTime     생성 시각의 기준(끝). 현재 시각을 쓰지 않는다
 * @param spanDays     기준 시각에서 과거로 며칠 동안의 데이터를 만들지
 * @param csvDir       CSV를 쓸 호스트 폴더. MySQL 컨테이너의 {@code mysqlFileDir}에 마운트돼
 *                     있어야 한다
 * @param mysqlFileDir MySQL 컨테이너 안에서 본 CSV 폴더({@code secure_file_priv})
 * @param method       h01 적재 방식
 * @param chunkSize    한 트랜잭션에 넣는 행 수. 모든 방식에서 같다
 * @param maxSeconds   중단 조건. 넘으면 멈추고 적재한 행 수로 전체 시간을 환산한다
 */
@ConfigurationProperties("datagen")
public record DatagenProperties(
		@DefaultValue("csv") String job,
		@DefaultValue("20261111") long seed,
		@DefaultValue("1000000") long rows,
		@DefaultValue("1.0") double scale,
		@DefaultValue("2026-11-01T00:00:00Z") Instant baseTime,
		@DefaultValue("300") int spanDays,
		@DefaultValue("data/csv") Path csvDir,
		@DefaultValue("/var/lib/mysql-files") String mysqlFileDir,
		LoadMethod method,
		@DefaultValue("1000") int chunkSize,
		@DefaultValue("1800") long maxSeconds) {

	public long scaledRows() {
		return Math.max(1, Math.round(rows * scale));
	}
}
