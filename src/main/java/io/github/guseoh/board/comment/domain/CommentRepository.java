package io.github.guseoh.board.comment.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByArticleIdOrderByIdAsc(Long articleId);
    void deleteAllByArticleId(Long articleId);
}
