// src/main/java/com/project/hrms/controller/MyShiftController.java
package com.project.hrms.controller;

import com.project.hrms.dto.ShiftAssignmentDTO;
import com.project.hrms.dto.ShiftRegisterDTO;
import com.project.hrms.service.ShiftAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/my-shifts")
@RequiredArgsConstructor
// Bảo vệ toàn bộ controller này, chỉ nhân viên (đã login) mới được vào
@PreAuthorize("hasAnyRole('USER', 'EMPLOYEE')")
public class MyShiftController {

    private final ShiftAssignmentService assignmentService;

    /**
     * API cho nhân viên tự đăng ký ca
     * (Khớp với 'POST /api/register-shift' của bạn)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerShift(
            @Valid @RequestBody ShiftRegisterDTO dto,
            Authentication authentication) {

        // Lấy username của người đang đăng nhập từ token
        String username = authentication.getName();

        ShiftAssignmentDTO result = assignmentService.employeeRegisterShift(dto, username);

        return ResponseEntity.ok(result);
    }
}