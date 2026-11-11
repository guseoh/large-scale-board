package io.github.guseoh.board.common.logging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfig {

	/**
	 * 추적 필터를 모든 필터보다 먼저 실행한다. 1단계에서 들어올 Spring Security 필터 체인보다도 앞이어야
	 * 인증 실패 로그에도 traceId가 붙는다.
	 */
	@Bean
	public FilterRegistrationBean<RequestTraceFilter> requestTraceFilter(LoggingProperties properties) {
		FilterRegistrationBean<RequestTraceFilter> registration =
				new FilterRegistrationBean<>(new RequestTraceFilter(properties.accessEnabled()));
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registration.addUrlPatterns("/*");
		return registration;
	}
}
