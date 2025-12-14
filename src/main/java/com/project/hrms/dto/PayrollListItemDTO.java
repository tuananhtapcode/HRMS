package com.project.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PayrollListItemDTO {
    private Long payrollId;
    private Long employeeId;
    private String employeeName;
    private BigDecimal totalSalary;
}
