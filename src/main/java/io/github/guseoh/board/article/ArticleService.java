package io.github.guseoh.board.article;

import io.github.guseoh.board.article.domain.Article;
import io.github.guseoh.board.article.domain.ArticleRepository;
import io.github.guseoh.board.article.web.ArticleRequest;
import io.github.guseoh.board.article.web.ArticleResponse;
import io.github.guseoh.board.comment.domain.CommentRepository;
import io.github.guseoh.board.global.exception.ForbiddenException;
import io.github.guseoh.board.global.exception.NotFoundException;
import io.github.guseoh.board.like.domain.ArticleLikeRepository;
import io.github.guseoh.board.member.MemberService;
import io.github.guseoh.board.member.domain.Member;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional(readOnly = true)
public class ArticleService {
    private final ArticleRepository articles; private final ArticleLikeRepository likes; private final CommentRepository comments; private final MemberService members;
    public ArticleService(ArticleRepository articles, ArticleLikeRepository likes, CommentRepository comments, MemberService members) { this.articles = articles; this.likes = likes; this.comments = comments; this.members = members; }
    @Transactional public ArticleResponse create(String username, ArticleRequest.Create request) { Article article = articles.save(new Article(members.find(username), request.title(), request.content())); return response(article); }
    public List<ArticleResponse> findAll() { return articles.findAllByOrderByIdDesc().stream().map(this::response).toList(); }
    public ArticleResponse findOne(Long id) { return response(find(id)); }
    @Transactional public ArticleResponse update(Long id, String username, ArticleRequest.Update request) { Article article = owned(id, username); article.update(request.title(), request.content()); return response(article); }
    @Transactional public void delete(Long id, String username) { Article article = owned(id, username); likes.deleteAllByArticleId(id); comments.deleteAllByArticleId(id); articles.delete(article); }
    public Article find(Long id) { return articles.findById(id).orElseThrow(() -> new NotFoundException("Article not found")); }
    private Article owned(Long id, String username) { Article article = find(id); if (!article.getMember().getUsername().equals(username)) throw new ForbiddenException("Article owner required"); return article; }
    private ArticleResponse response(Article article) { return ArticleResponse.from(article, likes.countByArticleId(article.getId())); }
}
