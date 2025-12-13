package com.project.hrms.controller;

import com.project.hrms.model.SystemSetting;
import com.project.hrms.repository.SystemSettingRepository;
import com.project.hrms.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SystemSettingRepository settingRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<SystemSetting>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success("Lấy cấu hình thành công", settingRepository.findAll()));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemSetting>> updateSetting(
            @PathVariable String key,
            @RequestBody Map<String, String> payload) {

        SystemSetting setting = settingRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình"));

        if (payload.containsKey("value")) setting.setValue(payload.get("value"));
        if (payload.containsKey("description")) setting.setDescription(payload.get("description"));

        SystemSetting updated = settingRepository.save(setting);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", updated));
    }
}