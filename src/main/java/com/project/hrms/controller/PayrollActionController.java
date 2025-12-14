package com.project.hrms.controller;

import com.project.hrms.dto.PayPayrollPeriodRequestDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.PayrollApprovalPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll-actions")
@RequiredArgsConstructor
public class PayrollActionController {

    private final PayrollApprovalPaymentService service;

    // ✅ SUMMARY (đúng cái FE đang gọi)
    @GetMapping("/periods/{periodId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN','HR','PAYROLL_MANAGER')")
    public ResponseEntity<?> summary(@PathVariable Long periodId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy tổng hợp chi trả kỳ lương thành công!", service.getSummary(periodId)));
    }

    // ✅ APPROVE PERIOD
    @PostMapping("/periods/{periodId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> approve(@PathVariable Long periodId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Duyệt kỳ lương thành công!", service.approvePeriod(periodId, auth.getName())));
    }

    // ✅ PAY PERIOD (bulk pay)
    @PostMapping("/periods/{periodId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> pay(@PathVariable Long periodId,
                                 @RequestBody(required = false) PayPayrollPeriodRequestDTO req,
                                 Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Chi trả kỳ lương thành công!", service.payPeriod(periodId, req, auth.getName())));
    }

    // ✅ PAY SINGLE (1 payroll)
    @PostMapping("/payrolls/{payrollId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER')")
    public ResponseEntity<?> paySingle(@PathVariable Long payrollId,
                                       @RequestBody(required = false) PayPayrollPeriodRequestDTO req,
                                       Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Chi trả payroll thành công!", service.paySingle(payrollId, req, auth.getName())));
    }
}
