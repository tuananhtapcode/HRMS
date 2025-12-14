package com.project.hrms.controller;

import com.project.hrms.dto.PayPayrollPeriodRequestDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.PayrollPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payrolls/payments")
@RequiredArgsConstructor
public class PayrollPaymentController {

    private final PayrollPaymentService paymentService;

    // TODO: tuỳ hệ thống bạn đang lấy userId ở đâu (Account/Employee)
    private Long currentUserId() { return 1L; }
    private String currentUserName() { return "SYSTEM"; }

    // 1) Summary kỳ (màn tổng quan)
    @GetMapping("/periods/{periodId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER','FINANCE')")
    public ResponseEntity<?> summary(@PathVariable Long periodId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy tổng quan chi trả kỳ lương thành công!",
                paymentService.getSummary(periodId)));
    }

    // 2) Approve kỳ
    @PatchMapping("/periods/{periodId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> approve(@PathVariable Long periodId) {
        int affected = paymentService.approvePeriod(periodId, currentUserId(), currentUserName());
        return ResponseEntity.ok(ApiResponse.success("Approve kỳ lương thành công!", Map.of("affected", affected)));
    }

    // 3) Pay hàng loạt (paidAmount = totalSalary)
    @PatchMapping("/periods/{periodId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER','FINANCE')")
    public ResponseEntity<?> payPeriod(@PathVariable Long periodId,
                                       @RequestBody PayPayrollPeriodRequestDTO req) {
        int affected = paymentService.payPeriodFull(periodId, req, currentUserId(), currentUserName());
        return ResponseEntity.ok(ApiResponse.success("Chi trả hàng loạt thành công!", Map.of("affected", affected)));
    }

    // 4) Pay từng nhân viên theo payrollId (hỗ trợ overridePaidAmount)
    @PatchMapping("/{payrollId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER','FINANCE')")
    public ResponseEntity<?> payOne(@PathVariable Long payrollId,
                                    @RequestBody PayPayrollPeriodRequestDTO req) {
        int affected = paymentService.payOne(payrollId, req, currentUserId(), currentUserName());
        return ResponseEntity.ok(ApiResponse.success("Chi trả nhân viên thành công!", Map.of("affected", affected)));
    }
}
