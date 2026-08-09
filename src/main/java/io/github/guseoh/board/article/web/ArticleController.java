package io.github.guseoh.board.article.web;

import io.github.guseoh.board.article.ArticleService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService service; public ArticleController(ArticleService service) { this.service = service; }
    @PostMapping public ResponseEntity<ArticleResponse> create(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody ArticleRequest.Create request) { ArticleResponse response = service.create(user.getUsername(), request); return ResponseEntity.created(URI.create("/api/articles/" + response.id())).body(response); }
    @GetMapping public List<ArticleResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public ArticleResponse findOne(@PathVariable Long id) { return service.findOne(id); }
    @PutMapping("/{id}") public ArticleResponse update(@PathVariable Long id, @AuthenticationPrincipal UserDetails user, @Valid @RequestBody ArticleRequest.Update request) { return service.update(id, user.getUsername(), request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) { service.delete(id, user.getUsername()); return ResponseEntity.noContent().build(); }
}
