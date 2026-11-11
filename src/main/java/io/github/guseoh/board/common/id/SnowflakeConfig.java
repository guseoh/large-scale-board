package io.github.guseoh.board.common.id;

import io.github.guseoh.board.id.SnowflakeIdGenerator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Snowflake 생성기를 앱 전체에서 하나만 쓰도록 빈으로 등록한다.
 *
 * <p>하나여야 하는 이유: 생성기 둘이 같은 노드 ID를 쓰면 같은 밀리초에 같은 순번을 내서 중복 ID가
 * 나온다. 엔티티 ID는 애플리케이션 서비스가 저장 전에 이 빈으로 할당한다. (D0-12, 1단계부터)
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SnowflakeProperties.class)
public class SnowflakeConfig {

	@Bean
	public SnowflakeIdGenerator snowflakeIdGenerator(SnowflakeProperties properties) {
		return new SnowflakeIdGenerator(properties.nodeId());
	}
}
