package com.knature.bo.controller;

import com.knature.common.domain.board.Article;
import com.knature.common.domain.board.Article.ArticleType;
import com.knature.common.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/** BO 게시판 관리 — 공지/이벤트/FAQ CRUD */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleAdminController {

    private final ArticleRepository articleRepository;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam ArticleType type) {
        return ResponseEntity.ok(articleRepository.findByTypeOrderByPinnedDescIdDesc(type));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        Article article = Article.builder()
                .type(ArticleType.valueOf(body.get("type")))
                .category(body.get("category"))
                .title(required(body.get("title"), "제목"))
                .content(required(body.get("content"), "내용"))
                .imageUrl(body.get("imageUrl"))
                .startDate(parseDate(body.get("startDate")))
                .endDate(parseDate(body.get("endDate")))
                .pinned("true".equals(body.get("pinned")))
                .build();
        return ResponseEntity.ok(articleRepository.save(article));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (body.get("title") != null) article.setTitle(body.get("title"));
        if (body.get("content") != null) article.setContent(body.get("content"));
        if (body.get("category") != null) article.setCategory(body.get("category"));
        if (body.get("imageUrl") != null) article.setImageUrl(body.get("imageUrl"));
        if (body.containsKey("startDate")) article.setStartDate(parseDate(body.get("startDate")));
        if (body.containsKey("endDate")) article.setEndDate(parseDate(body.get("endDate")));
        if (body.get("pinned") != null) article.setPinned("true".equals(body.get("pinned")));
        if (body.get("visible") != null) article.setVisible("true".equals(body.get("visible")));
        return ResponseEntity.ok(article);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        articleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }

    private String required(String v, String label) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(label + "을(를) 입력해주세요.");
        return v;
    }

    private LocalDate parseDate(String v) {
        return v == null || v.isBlank() ? null : LocalDate.parse(v);
    }
}
