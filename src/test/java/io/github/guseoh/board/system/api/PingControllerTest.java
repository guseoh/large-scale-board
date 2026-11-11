package io.github.guseoh.board.system.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guseoh.board.TestcontainersConfiguration;
import io.github.guseoh.board.common.logging.TraceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 실제 MySQL(Testcontainers)과 함께 앱 전체를 띄워 ping API와 추적 필터 등록을 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void ping은_DB_없이_UP을_준다() throws Exception {
		mockMvc.perform(get("/api/v1/ping"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				// 필터가 FilterRegistrationBean으로 실제 등록됐는지 확인한다
				.andExpect(header().string(TraceContext.HEADER, org.hamcrest.Matchers.matchesPattern("^[0-9a-f]{32}$")));
	}

	@Test
	void ping_db는_커넥션_풀을_거쳐_SELECT_1을_실행한다() throws Exception {
		mockMvc.perform(get("/api/v1/ping/db").header(TraceContext.HEADER, "ping-db-trace-01"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(header().string(TraceContext.HEADER, "ping-db-trace-01"));
	}
}
