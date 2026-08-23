package io.github.guseoh.board.like.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    boolean existsByArticleIdAndMemberId(Long articleId, Long memberId);
    Optional<ArticleLike> findByArticleIdAndMemberId(Long articleId, Long memberId);
    long countByArticleId(Long articleId);
    void deleteAllByArticleId(Long articleId);
}
