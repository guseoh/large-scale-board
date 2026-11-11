package io.github.guseoh.datagen;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 데이터 생성기 진입점. 웹 서버를 띄우지 않고({@code spring.main.web-application-type=none}), 작업
 * 하나를 끝내면 종료한다.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class DatagenApplication {

	public static void main(String[] args) {
		// 생성 시각(LocalDateTime)은 UTC 값이다. hibernate.jdbc.time_zone=UTC는 LocalDateTime을
		// "JVM 기본 시간대의 시각"으로 보고 UTC로 바꿔 저장한다. 기본 시간대가 KST면 JPA로 넣은
		// 값만 9시간 앞당겨져 CSV·JDBC로 넣은 값과 달라진다. JVM 기본 시간대를 UTC로 고정해 세
		// 경로가 같은 값을 넣게 한다.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		// 작업이 끝나면 커넥션 풀 등을 정리하고 종료 코드를 돌려준다. 예외로 끝나면 0이 아닌 값이
		// 나와 스크립트가 알아챈다.
		System.exit(SpringApplication.exit(SpringApplication.run(DatagenApplication.class, args)));
	}
}
