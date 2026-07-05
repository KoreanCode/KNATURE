package com.knature.bo.service;

import com.knature.common.domain.admin.Admin;
import com.knature.common.domain.admin.AdminRole;
import com.knature.common.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Admin> getAdmins() {
        return adminRepository.findAll();
    }

    @Transactional
    public Admin createAdmin(String username, String password, String name, AdminRole role) {
        if (username == null || username.isBlank() || password == null || password.isBlank()
                || name == null || name.isBlank()) {
            throw new IllegalArgumentException("아이디/비밀번호/이름을 모두 입력해주세요.");
        }
        if (adminRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + username);
        }
        return adminRepository.save(Admin.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role(role != null ? role : AdminRole.SUPER_ADMIN)
                .build());
    }

    /** 이름/비밀번호 수정 (비밀번호는 입력된 경우에만 변경) */
    @Transactional
    public Admin updateAdmin(Long id, String name, String password) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다: " + id));
        if (name != null && !name.isBlank()) {
            admin.setName(name);
        }
        if (password != null && !password.isBlank()) {
            admin.setPassword(passwordEncoder.encode(password));
        }
        return admin;
    }

    /** 삭제 — 자기 자신은 삭제 불가 */
    @Transactional
    public void deleteAdmin(Long id, String currentUsername) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다: " + id));
        if (admin.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("본인 계정은 삭제할 수 없습니다.");
        }
        adminRepository.delete(admin);
    }
}
