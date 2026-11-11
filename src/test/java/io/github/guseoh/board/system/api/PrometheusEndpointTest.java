package io.github.guseoh.board.system.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guseoh.board.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 대시보드와 알림 규칙이 기대하는 지표가 /actuator/prometheus에 실제로 나오는지 확인한다.
 *
 * <p>Spring Boot는 테스트에서 지표 내보내기를 기본으로 끈다(외부로 지표를 보내지 않게). 그래서
 * {@code @AutoConfigureMetrics}로 켠다. 없으면 /actuator/prometheus가 404다.
 *
 * <p>PingControllerTest와 애너테이션 구성이 같아서 Spring 테스트 컨텍스트(와 MySQL 컨테이너)를 함께
 * 쓴다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
@Import(TestcontainersConfiguration.class)
class PrometheusEndpointTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void HikariCP_지표와_application_태그가_노출된다() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("hikaricp_connections_active")))
				.andExpect(content().string(containsString("hikaricp_connections_pending")))
				.andExpect(content().string(containsString("application=\"board\"")));
	}

	@Test
	void HTTP_요청_지표는_히스토그램_버킷으로_나온다() throws Exception {
		mockMvc.perform(get("/api/v1/ping")).andExpect(status().isOk());

		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("http_server_requests_seconds_bucket")));
	}
}
