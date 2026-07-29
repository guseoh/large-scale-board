package io.github.guseoh.board;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 시스템_ping은_인증_없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/api/system/ping"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void Actuator_health는_인증_없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void 보호된_경로는_인증이_없으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/private"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code")
                        .value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.path")
                        .value("/api/private"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void 관리자_경로는_ADMIN_권한이_없으면_403을_반환한다()
            throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code")
                        .value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.path")
                        .value("/api/admin/test"));
    }
}