package io.github.guseoh.board.comment;

import io.github.guseoh.board.article.ArticleService;
import io.github.guseoh.board.article.domain.Article;
import io.github.guseoh.board.comment.domain.Comment;
import io.github.guseoh.board.comment.domain.CommentRepository;
import io.github.guseoh.board.comment.web.CommentRequest;
import io.github.guseoh.board.comment.web.CommentResponse;
import io.github.guseoh.board.global.exception.ForbiddenException;
import io.github.guseoh.board.global.exception.NotFoundException;
import io.github.guseoh.board.member.MemberService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository comments; private final ArticleService articles; private final MemberService members;
    public CommentService(CommentRepository comments, ArticleService articles, MemberService members) { this.comments = comments; this.articles = articles; this.members = members; }
    @Transactional public CommentResponse create(Long articleId, String username, CommentRequest.Create request) { return CommentResponse.from(comments.save(new Comment(articles.find(articleId), members.find(username), request.content()))); }
    public List<CommentResponse> findAll(Long articleId) { articles.find(articleId); return comments.findAllByArticleIdOrderByIdAsc(articleId).stream().map(CommentResponse::from).toList(); }
    @Transactional public CommentResponse update(Long id, String username, CommentRequest.Update request) { Comment comment = owned(id, username); comment.update(request.content()); return CommentResponse.from(comment); }
    @Transactional public void delete(Long id, String username) { comments.delete(owned(id, username)); }
    private Comment owned(Long id, String username) { Comment comment = comments.findById(id).orElseThrow(() -> new NotFoundException("Comment not found")); if (!comment.getMember().getUsername().equals(username)) throw new ForbiddenException("Comment owner required"); return comment; }
}
