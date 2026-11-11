package io.github.guseoh.datagen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 데이터 생성기 진입점. 생성 규칙과 적재 코드는 D 묶음에서 추가한다.
 * 웹 서버를 띄우지 않는 설정은 application.yaml의 spring.main.web-application-type=none 이다.
 */
@SpringBootApplication
public class DatagenApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatagenApplication.class, args);
	}
}
