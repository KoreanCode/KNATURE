package com.knature.bo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/** 전역 예외 처리 — 내부 스택트레이스 노출 방지, 사용자 친화적 메시지 표준화 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 검증 실패 (잘못된 입력/상태) → 400 + 메시지 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    /** 경로 변수/파라미터 타입 불일치 (예: /orders/abc) → 400 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of("message", "잘못된 요청 경로 또는 파라미터입니다."));
    }

    /** 인증/인가 예외는 Security 필터 체인이 처리하도록 재던짐 */
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public void rethrowSecurity(Exception e) throws Exception {
        throw e;
    }

    /** 그 외 미처리 예외 → 500 + 일반 메시지 (내부 정보 비노출) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
