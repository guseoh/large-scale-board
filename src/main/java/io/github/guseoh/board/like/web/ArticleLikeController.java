package io.github.guseoh.board.like.web;
import io.github.guseoh.board.like.ArticleLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/articles/{articleId}/likes")
public class ArticleLikeController {
    private final ArticleLikeService service; public ArticleLikeController(ArticleLikeService service) { this.service = service; }
    @PostMapping public ResponseEntity<LikeResponse> create(@PathVariable Long articleId, @AuthenticationPrincipal UserDetails user) { return ResponseEntity.status(201).body(service.create(articleId, user.getUsername())); }
    @DeleteMapping public ResponseEntity<Void> delete(@PathVariable Long articleId, @AuthenticationPrincipal UserDetails user) { service.delete(articleId, user.getUsername()); return ResponseEntity.noContent().build(); }
}
