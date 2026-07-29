package io.github.guseoh.board;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	private static final DockerImageName MYSQL_IMAGE =
			DockerImageName.parse("mysql:8.4.11");

	@Bean
	@ServiceConnection	// Spring Boot가 Testcontainers의 MySQL 접속 정보를 자동으로 읽어 테스트용 DataSource를 구성한다.
	MySQLContainer mysqlContainer() {
		return new MySQLContainer(MYSQL_IMAGE)
				.withDatabaseName("large_scale_board_test")
				.withUsername("test")
				.withPassword("test");
	}
}