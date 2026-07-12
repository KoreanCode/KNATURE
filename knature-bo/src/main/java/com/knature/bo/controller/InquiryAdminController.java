package com.knature.bo.controller;

import com.knature.common.domain.board.Inquiry;
import com.knature.common.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/** BO 1:1 문의 관리 — 목록(미답변 필터) / 답변 작성 */
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryRepository inquiryRepository;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "false") boolean unanswered) {
        return ResponseEntity.ok(unanswered
                ? inquiryRepository.findByAnswerIsNullOrderByIdDesc()
                : inquiryRepository.findAllByOrderByIdDesc());
    }

    @PostMapping("/{id}/answer")
    @Transactional
    public ResponseEntity<?> answer(@PathVariable Long id, @RequestBody Map<String, String> body,
                                    Authentication auth) {
        String answer = body.get("answer");
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
        inquiry.setAnswer(answer);
        inquiry.setAnsweredAt(LocalDateTime.now());
        inquiry.setAnsweredBy(auth.getName());
        return ResponseEntity.ok(Map.of("message", "답변이 등록되었습니다."));
    }
}
