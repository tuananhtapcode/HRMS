package com.project.hrms.controller;

import com.project.hrms.dto.ShiftDTO;
import com.project.hrms.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Đây chính là API "timeworking" của bạn
@RequestMapping("/api/v1/timeworking/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    // API tạo ca làm việc (Admin-only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShiftDTO> createShift(@Valid @RequestBody ShiftDTO dto) {
        return ResponseEntity.ok(shiftService.createShift(dto));
    }

    // API cập nhật ca (Admin-only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShiftDTO> updateShift(@PathVariable Long id, @Valid @RequestBody ShiftDTO dto) {
        return ResponseEntity.ok(shiftService.updateShift(id, dto));
    }

    // API xóa ca (Admin-only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.ok("Xóa ca làm việc thành công.");
    }

    // API lấy danh sách ca (Cho Admin/Manager xem để chọn)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Hoặc "isAuthenticated()"
    public ResponseEntity<List<ShiftDTO>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    // API lấy chi tiết 1 ca (shift-detail)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Hoặc "isAuthenticated()"
    public ResponseEntity<ShiftDTO> getShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }
}