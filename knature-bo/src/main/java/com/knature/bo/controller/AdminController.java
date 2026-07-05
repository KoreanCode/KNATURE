package com.knature.bo.controller;

import com.knature.bo.service.AdminService;
import com.knature.common.domain.admin.AdminRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(adminService.getAdmins());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        AdminRole role = body.get("role") != null ? AdminRole.valueOf(body.get("role")) : AdminRole.SUPER_ADMIN;
        return ResponseEntity.ok(adminService.createAdmin(
                body.get("username"), body.get("password"), body.get("name"), role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.updateAdmin(id, body.get("name"), body.get("password")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        adminService.deleteAdmin(id, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "계정이 삭제되었습니다."));
    }
}
