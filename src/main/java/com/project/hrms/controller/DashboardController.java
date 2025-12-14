package com.project.hrms.controller;

import com.project.hrms.dto.PayrollOverviewDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.PayrollOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PayrollOverviewService payrollOverviewService;

    @GetMapping("/payroll-overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'PAYROLL_MANAGER')")
    public ResponseEntity<ApiResponse<PayrollOverviewDTO>> getPayrollOverview() {
        PayrollOverviewDTO overview = payrollOverviewService.getPayrollOverview();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tổng quan lương thành công!", overview));
    }
}
