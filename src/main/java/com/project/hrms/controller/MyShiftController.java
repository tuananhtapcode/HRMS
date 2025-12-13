package com.project.hrms.controller;

import com.project.hrms.dto.ShiftAssignmentDTO;
import com.project.hrms.dto.ShiftRegisterDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.ShiftAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/my-shifts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'EMPLOYEE')")
public class MyShiftController {

    private final ShiftAssignmentService assignmentService;

    /**
     * API cho nhân viên tự đăng ký ca
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ShiftAssignmentDTO>> registerShift(
            @Valid @RequestBody ShiftRegisterDTO dto,
            BindingResult bindingResult,
            Authentication authentication) {

        // 1. Kiểm tra lỗi Validation
        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage));
        }

        // 2. Lấy username từ token
        String username = authentication.getName();

        // 3. Gọi service
        ShiftAssignmentDTO result = assignmentService.employeeRegisterShift(dto, username);

        return ResponseEntity.ok(ApiResponse.success("Đăng ký ca thành công", result));
    }
}