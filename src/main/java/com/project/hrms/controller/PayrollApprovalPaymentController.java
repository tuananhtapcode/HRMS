package com.project.hrms.controller;

import com.project.hrms.dto.PayPayrollPeriodRequestDTO;
import com.project.hrms.dto.PayrollActionResponseDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.PayrollApprovalPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll-periods")
@RequiredArgsConstructor
public class PayrollApprovalPaymentController {

    private final PayrollApprovalPaymentService approvalPaymentService;

    // ✅ Duyệt kỳ lương
    @PatchMapping("/{periodId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER','HR')")
    public ResponseEntity<ApiResponse<PayrollActionResponseDTO>> approvePeriod(@PathVariable Long periodId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PayrollActionResponseDTO res = approvalPaymentService.approvePeriod(periodId, username);
        return ResponseEntity.ok(ApiResponse.success("Duyệt kỳ lương thành công!", res));
    }

    // ✅ Chi trả kỳ lương
    @PostMapping("/{periodId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','PAYROLL_MANAGER','ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PayrollActionResponseDTO>> payPeriod(
            @PathVariable Long periodId,
            @RequestBody(required = false) PayPayrollPeriodRequestDTO req
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PayrollActionResponseDTO res = approvalPaymentService.payPeriod(periodId, req, username);
        return ResponseEntity.ok(ApiResponse.success("Chi trả kỳ lương thành công!", res));
    }
}
