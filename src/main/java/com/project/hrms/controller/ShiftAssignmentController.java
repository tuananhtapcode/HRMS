package com.project.hrms.controller;

import com.project.hrms.dto.BulkAssignDTO;
import com.project.hrms.dto.ShiftAssignmentDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.ShiftAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/shift-assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ShiftAssignmentController {

    private final ShiftAssignmentService assignmentService;

    // API chính: Phân ca cho nhân viên
    @PostMapping
    public ResponseEntity<ApiResponse<ShiftAssignmentDTO>> assignShift(
            @Valid @RequestBody ShiftAssignmentDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage));
        }

        ShiftAssignmentDTO result = assignmentService.assignShift(dto);
        return ResponseEntity.ok(ApiResponse.success("Phân ca thành công", result));
    }

    // API Lấy lịch làm việc của nhân viên
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isOwner(authentication, #employeeId)")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDTO>>> getAssignments(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ShiftAssignmentDTO> list = assignmentService.getAssignmentsByEmployee(employeeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch làm việc thành công", list));
    }

    // API Xóa phân ca
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa phân ca thành công", null));
    }

    // API Phân ca hàng loạt
    @PostMapping("/bulk-by-department")
    public ResponseEntity<ApiResponse<Void>> bulkAssignByDepartment(
            @Valid @RequestBody BulkAssignDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage));
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, "Ngày bắt đầu không thể sau ngày kết thúc"));
        }

        assignmentService.bulkAssignByDepartment(dto);
        return ResponseEntity.ok(ApiResponse.success("Phân ca hàng loạt cho phòng ban thành công", null));
    }
}