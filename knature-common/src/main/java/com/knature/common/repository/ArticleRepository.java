package com.knature.common.repository;

import com.knature.common.domain.board.Article;
import com.knature.common.domain.board.Article.ArticleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByTypeOrderByPinnedDescIdDesc(ArticleType type);

    List<Article> findByTypeAndVisibleTrueOrderByPinnedDescIdDesc(ArticleType type);
}
