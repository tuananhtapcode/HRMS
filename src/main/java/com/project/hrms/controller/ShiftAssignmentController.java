// src/main/java/com/project/hrms/controller/ShiftAssignmentController.java
package com.project.hrms.controller;

import com.project.hrms.dto.BulkAssignDTO;
import com.project.hrms.dto.ShiftAssignmentDTO;
import com.project.hrms.service.ShiftAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
// Đây chính là API "register-shift" của bạn
@RequestMapping("/api/v1/shift-assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Chỉ Admin/Manager mới được phân ca
public class ShiftAssignmentController {

    private final ShiftAssignmentService assignmentService;

    // API chính: Phân ca cho nhân viên (Tạo mới hoặc Cập nhật)
    @PostMapping
    public ResponseEntity<ShiftAssignmentDTO> assignShift(@Valid @RequestBody ShiftAssignmentDTO dto) {
        return ResponseEntity.ok(assignmentService.assignShift(dto));
    }

    // API Lấy lịch làm việc của nhân viên (theo tháng)
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isOwner(authentication, #employeeId)")
    public ResponseEntity<List<ShiftAssignmentDTO>> getAssignments(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByEmployee(employeeId, startDate, endDate));
    }

    // API Xóa phân ca
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok("Xóa phân ca thành công.");
    }
    // --- API MỚI CHO PHÂN CA HÀNG LOẠT ---
    @PostMapping("/bulk-by-department")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> bulkAssignByDepartment(@Valid @RequestBody BulkAssignDTO dto) {

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            return ResponseEntity.badRequest().body("Ngày bắt đầu không thể sau ngày kết thúc.");
        } assignmentService.bulkAssignByDepartment(dto);

        return ResponseEntity.ok("Phân ca hàng loạt cho phòng ban thành công.");
    }

}
