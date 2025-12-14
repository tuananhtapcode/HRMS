package com.project.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollListItemDTO {
    private Long payrollId;
    private Long employeeId;
    private String employeeName;
    private BigDecimal totalSalary;

    // 👇 THÊM 2 TRƯỜNG NÀY
    private BigDecimal personalIncomeTax;
    private BigDecimal insuranceDeduction;
}
