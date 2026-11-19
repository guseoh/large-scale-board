package io.github.guseoh.board.member.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guseoh.board.TestcontainersConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// 컨텍스트를 PingControllerTest와 함께 쓰도록 같은 값을 준다.
@SpringBootTest(properties = "management.server.port=")
@AutoConfigureMockMvc
@AutoConfigureMetrics
@Import(TestcontainersConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class MemberControllerTest {

	private final MockMvc mockMvc;
	private final JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("delete from member");
	}

	@Test
	@DisplayName("가입하면 201과 함께 ID가 문자열인 회원 정보를 주고 비밀번호는 없다")
	void signUpReturnsCreated() throws Exception {
		signUp("""
				{"email": "Hong@Example.com", "password": "password123", "nickname": "길동"}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(matchesPattern("^\\d{15,19}$")))
				.andExpect(jsonPath("$.email").value("hong@example.com"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andExpect(content().string(org.hamcrest.Matchers.not(
						org.hamcrest.Matchers.containsString("password123"))));
	}

	@Test
	@DisplayName("이미 가입한 이메일이면 409와 MEMBER_EMAIL_DUPLICATED")
	void duplicateEmailReturnsConflict() throws Exception {
		String body = """
				{"email": "a@example.com", "password": "password123", "nickname": "길동"}
				""";
		signUp(body).andExpect(status().isCreated());

		signUp(body)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("MEMBER_EMAIL_DUPLICATED"))
				.andExpect(jsonPath("$.traceId").isNotEmpty());
	}

	@Test
	@DisplayName("형식이 틀린 요청은 400과 필드별 사유를 준다")
	void invalidRequestReturnsFieldErrors() throws Exception {
		signUp("""
				{"email": "not-an-email", "password": "", "nickname": ""}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
				.andExpect(jsonPath("$.errors[*].field").value(hasItem("password")))
				.andExpect(jsonPath("$.errors[*].field").value(hasItem("nickname")));
	}

	@Test
	@DisplayName("비밀번호가 정책에 맞지 않으면 400과 MEMBER_INVALID_PASSWORD_POLICY")
	void weakPasswordReturnsBadRequest() throws Exception {
		signUp("""
				{"email": "a@example.com", "password": "short", "nickname": "길동"}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MEMBER_INVALID_PASSWORD_POLICY"));
	}

	private ResultActions signUp(String json) throws Exception {
		return mockMvc.perform(post("/api/v1/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}
}
