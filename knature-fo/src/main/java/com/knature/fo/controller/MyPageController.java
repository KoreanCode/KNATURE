package com.knature.fo.controller;

import com.knature.common.domain.member.Member;
import com.knature.common.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private Member me(Authentication auth) {
        return memberRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    @GetMapping("/info")
    public ResponseEntity<?> info(Authentication auth) {
        return ResponseEntity.ok(me(auth));
    }

    /** 정보 수정 전 비밀번호 재확인 */
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> body, Authentication auth) {
        Member member = me(auth);
        if (!passwordEncoder.matches(body.get("password"), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return ResponseEntity.ok(Map.of("verified", true));
    }

    /** 기본 정보 수정 (이메일/휴대폰/주소) */
    @PutMapping("/info")
    @Transactional
    public ResponseEntity<?> updateInfo(@RequestBody Map<String, String> body, Authentication auth) {
        Member member = me(auth);
        if (body.get("email") != null && !body.get("email").isBlank()) member.setEmail(body.get("email"));
        if (body.get("phone") != null) member.setPhone(body.get("phone"));
        if (body.get("zipcode") != null) member.setZipcode(body.get("zipcode"));
        if (body.get("address") != null) member.setAddress(body.get("address"));
        if (body.get("addressDetail") != null) member.setAddressDetail(body.get("addressDetail"));
        return ResponseEntity.ok(Map.of("message", "회원 정보가 수정되었습니다."));
    }

    /** 비밀번호 변경 (현재 비밀번호 확인 후) */
    @PutMapping("/password")
    @Transactional
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication auth) {
        Member member = me(auth);
        if (!passwordEncoder.matches(body.get("currentPassword"), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상으로 입력해주세요.");
        }
        member.setPassword(passwordEncoder.encode(newPassword));
        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
    }

    /** 회원 탈퇴 — active=false 처리 (주문 이력 보존) */
    @PostMapping("/withdraw")
    @Transactional
    public ResponseEntity<?> withdraw(@RequestBody Map<String, String> body, Authentication auth,
                                      HttpSession session) {
        Member member = me(auth);
        if (!passwordEncoder.matches(body.get("password"), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        member.setActive(false);
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다."));
    }
}
