package com.knature.bo.controller;

import com.knature.bo.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(settingService.getSettings());
    }

    @PutMapping
    public ResponseEntity<?> saveSettings(@RequestBody Map<String, String> settings) {
        settingService.saveSettings(settings);
        return ResponseEntity.ok(Map.of("message", "설정이 저장되었습니다."));
    }
}
