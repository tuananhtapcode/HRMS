package com.project.hrms.controller;

import com.project.hrms.dto.SalaryProfileUpsertDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.SalaryProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/salary-profiles")
@RequiredArgsConstructor
public class SalaryProfileController {

    private final SalaryProfileService salaryProfileService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> list(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy salary profile theo nhân viên thành công!", salaryProfileService.listByEmployee(employeeId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> upsert(@PathVariable Long employeeId,
                                    @Valid @RequestBody SalaryProfileUpsertDTO dto,
                                    BindingResult br) {
        if (br.hasErrors()) return ResponseEntity.badRequest().body(br.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList());
        return ResponseEntity.ok(ApiResponse.success("Gán/cập nhật salary profile thành công!", salaryProfileService.upsert(employeeId, dto)));
    }

    @PutMapping("/{profileId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> update(@PathVariable Long employeeId,
                                    @PathVariable Long profileId,
                                    @Valid @RequestBody SalaryProfileUpsertDTO dto,
                                    BindingResult br) {
        if (br.hasErrors()) return ResponseEntity.badRequest().body(br.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật salary profile thành công!", salaryProfileService.update(employeeId, profileId, dto)));
    }
}
