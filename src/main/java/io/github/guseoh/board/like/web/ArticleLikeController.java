package io.github.guseoh.board.like.web;

import io.github.guseoh.board.like.ArticleLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/{articleId}/likes")
@RequiredArgsConstructor
public class ArticleLikeController {
    private final ArticleLikeService service;

    @PostMapping
    public ResponseEntity<LikeResponse> create(@PathVariable Long articleId,
                                                @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(201).body(service.create(articleId, user.getUsername()));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long articleId,
                                       @AuthenticationPrincipal UserDetails user) {
        service.delete(articleId, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
