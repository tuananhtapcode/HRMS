package com.project.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PayrollPeriodPaymentSummaryDTO {
    private Long periodId;
    private String periodName;
    private Long headcount;
    private BigDecimal totalToPay;     // SUM totalSalary
    private BigDecimal totalPaid;      // SUM paidAmount
    private Long paidCount;            // số payroll PAID
    private Long approvedCount;        // số payroll APPROVED
    private Long calculatedCount;      // số payroll CALCULATED
}
