package com.knature.fo.controller;

import com.knature.common.domain.member.Member;
import com.knature.fo.service.MemberAuthService;
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
    private final MemberAuthService memberAuthService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        Member member = memberAuthService.signup(
                body.get("username"), body.get("password"), body.get("name"),
                body.get("email"), body.get("phone"),
                body.get("zipcode"), body.get("address"), body.get("addressDetail"));
        return ResponseEntity.ok(Map.of(
                "message", "회원가입이 완료되었습니다.",
                "username", member.getUsername(),
                "name", member.getName()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpSession session) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.get("username"), request.get("password"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        Member member = memberAuthService.getByUsername(request.get("username"));
        return ResponseEntity.ok(memberSummary(member));
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
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        Member member = memberAuthService.getByUsername(auth.getName());
        return ResponseEntity.ok(memberSummary(member));
    }

    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody Map<String, String> body) {
        String masked = memberAuthService.findId(body.get("name"), body.get("email"));
        return ResponseEntity.ok(Map.of("username", masked));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String tempPassword = memberAuthService.resetPassword(body.get("username"), body.get("email"));
        return ResponseEntity.ok(Map.of(
                "message", "임시 비밀번호가 발급되었습니다. 로그인 후 반드시 변경해주세요.",
                "tempPassword", tempPassword
        ));
    }

    private Map<String, Object> memberSummary(Member member) {
        return Map.of(
                "id", member.getId(),
                "username", member.getUsername(),
                "name", member.getName(),
                "email", member.getEmail(),
                "grade", member.getGrade().name(),
                "gradeLabel", member.getGrade().getLabel()
        );
    }
}
