package io.github.guseoh.board.comment.web;

import io.github.guseoh.board.comment.CommentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService service;

    @PostMapping("/articles/{articleId}/comments")
    public ResponseEntity<CommentResponse> create(@PathVariable Long articleId,
                                                  @AuthenticationPrincipal UserDetails user,
                                                  @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = service.create(articleId, user.getUsername(), request);
        return ResponseEntity.created(URI.create("/api/comments/" + response.id())).body(response);
    }

    @GetMapping("/articles/{articleId}/comments")
    public List<CommentResponse> findAll(@PathVariable Long articleId) {
        return service.findAll(articleId);
    }

    @PutMapping("/comments/{id}")
    public CommentResponse update(@PathVariable Long id, @AuthenticationPrincipal UserDetails user,
                                  @Valid @RequestBody CommentUpdateRequest request) {
        return service.update(id, user.getUsername(), request);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        service.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
