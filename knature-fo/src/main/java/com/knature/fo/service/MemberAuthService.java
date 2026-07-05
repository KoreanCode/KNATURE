package com.knature.fo.service;

import com.knature.common.domain.member.Member;
import com.knature.common.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TEMP_PW_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    @Transactional
    public Member signup(String username, String password, String name, String email, String phone,
                         String zipcode, String address, String addressDetail) {
        if (username == null || username.isBlank() || password == null || password.isBlank()
                || name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("필수 정보(아이디/비밀번호/이름/이메일)를 모두 입력해주세요.");
        }
        if (username.length() < 4 || username.length() > 20) {
            throw new IllegalArgumentException("아이디는 4~20자로 입력해주세요.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상으로 입력해주세요.");
        }
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        member.setZipcode(zipcode);
        member.setAddress(address);
        member.setAddressDetail(addressDetail);
        return memberRepository.save(member);
    }

    public Member getByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    /** 아이디 찾기 — 이름+이메일 일치 시 마스킹된 아이디 반환 */
    public String findId(String name, String email) {
        Member member = memberRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보가 없습니다."));
        return maskUsername(member.getUsername());
    }

    /**
     * 비밀번호 재설정 — 아이디+이메일 일치 시 임시 비밀번호 발급.
     * 1차: 메일 발송 인프라 부재로 화면에 1회 표시 (2차에서 이메일/SMS 발송으로 전환)
     */
    @Transactional
    public String resetPassword(String username, String email) {
        Member member = memberRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보가 없습니다."));
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(TEMP_PW_CHARS.charAt(RANDOM.nextInt(TEMP_PW_CHARS.length())));
        }
        String tempPassword = sb.toString();
        member.setPassword(passwordEncoder.encode(tempPassword));
        return tempPassword;
    }

    private String maskUsername(String username) {
        if (username.length() <= 3) return username.charAt(0) + "**";
        int visible = username.length() - 3;
        return username.substring(0, visible) + "***";
    }
}
