package com.project.hrms.controller;

import com.project.hrms.dto.PayrollPeriodCreateDTO;
import com.project.hrms.model.PayrollPeriod;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.PayrollPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll-periods")
@RequiredArgsConstructor
public class PayrollPeriodController {

    private final PayrollPeriodService payrollPeriodService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> create(@Valid @RequestBody PayrollPeriodCreateDTO dto, BindingResult br) {
        if (br.hasErrors()) return ResponseEntity.badRequest().body(br.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList());
        return ResponseEntity.ok(ApiResponse.success("Tạo kỳ lương thành công!", payrollPeriodService.create(dto)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kỳ lương thành công!", payrollPeriodService.getAll()));
    }

    // Thêm một API để lấy kỳ lương theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy kỳ lương thành công!", payrollPeriodService.getById(id)));
    }


    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Đóng kỳ lương thành công!", payrollPeriodService.close(id)));
    }
}
