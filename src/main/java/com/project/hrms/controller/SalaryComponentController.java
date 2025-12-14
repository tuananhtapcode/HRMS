package com.project.hrms.controller;

import com.project.hrms.dto.SalaryComponentCreateDTO;
import com.project.hrms.dto.SalaryComponentUpdateDTO;
import com.project.hrms.model.enums.SalaryComponentType;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.SalaryComponentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/salary-components")
@RequiredArgsConstructor
public class SalaryComponentController {

    private final SalaryComponentService salaryComponentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> create(@Valid @RequestBody SalaryComponentCreateDTO dto, BindingResult br) {
        if (br.hasErrors()) return ResponseEntity.badRequest().body(br.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList());
        return ResponseEntity.ok(ApiResponse.success("Tạo salary component thành công!", salaryComponentService.create(dto)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> list(@RequestParam(required = false) SalaryComponentType type) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách salary component thành công!", salaryComponentService.list(type)));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái active thành công!", salaryComponentService.toggleActive(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SalaryComponentUpdateDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật salary component thành công!", salaryComponentService.update(id, dto)));
    }

    // API Xóa mềm
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        salaryComponentService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa thành phần lương thành công!", null));
    }
}