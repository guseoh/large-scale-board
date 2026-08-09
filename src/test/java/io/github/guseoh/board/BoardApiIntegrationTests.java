package io.github.guseoh.board;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import io.github.guseoh.board.article.domain.ArticleRepository;
import io.github.guseoh.board.comment.domain.CommentRepository;
import io.github.guseoh.board.like.domain.ArticleLikeRepository;
import io.github.guseoh.board.member.domain.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@Import(TestcontainersConfiguration.class)
class BoardApiIntegrationTests {
    @Autowired WebApplicationContext context;
    @Autowired ArticleLikeRepository likes;
    @Autowired CommentRepository comments;
    @Autowired ArticleRepository articles;
    @Autowired MemberRepository members;
    private MockMvc mvc;
    private MockMvc mvc() { if (mvc == null) mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); return mvc; }
    @AfterEach void cleanUp() { likes.deleteAll(); comments.deleteAll(); articles.deleteAll(); members.deleteAll(); }
    @Test void memberArticleCommentAndLikeCrudRespectAuthenticatedOwner() throws Exception {
        register("writer", "writer@example.com"); register("reader", "reader@example.com");
        long articleId = id(mvc().perform(post("/api/articles").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("writer", "password123")).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"first\",\"content\":\"content\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("writer")).andReturn().getResponse().getContentAsString());
        mvc().perform(get("/api/articles/{id}", articleId).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("reader", "password123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("first"));
        mvc().perform(put("/api/articles/{id}", articleId).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("reader", "password123")).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"changed\",\"content\":\"content\"}"))
            .andExpect(status().isForbidden());
        long commentId = id(mvc().perform(post("/api/articles/{id}/comments", articleId).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("reader", "password123")).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"nice\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc().perform(get("/api/articles/{id}/comments", articleId).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("writer", "password123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(commentId));
        mvc().perform(post("/api/articles/{id}/likes", articleId).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("reader", "password123")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.likeCount").value(1));
        mvc().perform(post("/api/articles/{id}/likes", articleId).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("reader", "password123")))
            .andExpect(status().isConflict());
        mvc().perform(delete("/api/comments/{id}", commentId).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("writer", "password123")))
            .andExpect(status().isForbidden());
        mvc().perform(delete("/api/articles/{id}", articleId).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("writer", "password123")))
            .andExpect(status().isNoContent());
    }
    private void register(String username, String email) throws Exception { mvc().perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"password123\"}")).andExpect(status().isCreated()); }
    private long id(String json) { return Long.parseLong(json.replaceFirst(".*\\\"id\\\":(\\d+).*", "$1")); }
}
