package com.project.hrms.controller;

import com.project.hrms.dto.PayrollCalculateRequestDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> calculate(@Valid @RequestBody PayrollCalculateRequestDTO dto, BindingResult br) {
        if (br.hasErrors()) return ResponseEntity.badRequest().body(br.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList());
        return ResponseEntity.ok(ApiResponse.success("Tính lương thành công!", payrollService.calculatePayrollForEmployee(dto)));
    }

    @PostMapping("/calculate-batch")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> calculateBatch(@RequestParam Long periodId,
                                            @RequestParam int month,
                                            @RequestParam int year) {
        int count = payrollService.calculateBatch(periodId, month, year);
        return ResponseEntity.ok(ApiResponse.success("Tính lương batch thành công!", Map.of("processedEmployees", count)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> list(@RequestParam Long periodId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "payrollId"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bảng lương theo kỳ thành công!",
                payrollService.listPayrollByPeriod(periodId, pageable)));
    }

    @GetMapping("/{payrollId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> detail(@PathVariable Long payrollId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết bảng lương thành công!", payrollService.getPayrollDetail(payrollId)));
    }
}
