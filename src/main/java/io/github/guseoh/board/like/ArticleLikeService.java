package io.github.guseoh.board.like;

import io.github.guseoh.board.article.ArticleService;
import io.github.guseoh.board.article.domain.Article;
import io.github.guseoh.board.global.exception.ConflictException;
import io.github.guseoh.board.global.exception.NotFoundException;
import io.github.guseoh.board.like.domain.ArticleLike;
import io.github.guseoh.board.like.domain.ArticleLikeRepository;
import io.github.guseoh.board.like.web.LikeResponse;
import io.github.guseoh.board.member.MemberService;
import io.github.guseoh.board.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArticleLikeService {
    private final ArticleLikeRepository likes;
    private final ArticleService articles;
    private final MemberService members;

    public ArticleLikeService(ArticleLikeRepository likes, ArticleService articles, MemberService members) {
        this.likes = likes;
        this.articles = articles;
        this.members = members;
    }

    @Transactional
    public LikeResponse create(Long articleId, String username) {
        Article article = articles.find(articleId);
        Member member = members.find(username);
        if (likes.existsByArticleIdAndMemberId(articleId, member.getId())) {
            throw new ConflictException("Article already liked");
        }
        likes.save(new ArticleLike(article, member));
        return response(articleId);
    }

    @Transactional
    public void delete(Long articleId, String username) {
        Member member = members.find(username);
        ArticleLike like = likes.findByArticleIdAndMemberId(articleId, member.getId())
                .orElseThrow(() -> new NotFoundException("Like not found"));
        likes.delete(like);
    }

    private LikeResponse response(Long articleId) {
        return new LikeResponse(articleId, likes.countByArticleId(articleId));
    }
}
