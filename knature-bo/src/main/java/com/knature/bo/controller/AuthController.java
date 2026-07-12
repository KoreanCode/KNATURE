package com.knature.bo.controller;

import com.knature.common.domain.admin.Admin;
import com.knature.common.repository.AdminRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AdminRepository adminRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpSession session) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.get("username"), request.get("password"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        Admin admin = adminRepository.findByUsername(request.get("username")).orElseThrow();
        return ResponseEntity.ok(adminSummary(admin));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "인증되지 않았습니다."));
        }
        Admin admin = adminRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(adminSummary(admin));
    }

    /** 응답 공통 — 공장관리자는 소속 공장 정보 포함 (프론트 메뉴/스코프 분기용) */
    private Map<String, Object> adminSummary(Admin admin) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("name", admin.getName());
        result.put("role", admin.getRole().name());
        if (admin.getFactory() != null) {
            result.put("factoryId", admin.getFactory().getId());
            result.put("factoryName", admin.getFactory().getName());
        }
        return result;
    }
}
