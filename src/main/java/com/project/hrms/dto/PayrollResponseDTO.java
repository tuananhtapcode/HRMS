package com.project.hrms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PayrollResponseDTO {
    private Long payrollId;
    private Long employeeId;
    private Long payrollPeriodId;
    private BigDecimal totalSalary;
    private List<PayrollItemResponseDTO> items;
}
