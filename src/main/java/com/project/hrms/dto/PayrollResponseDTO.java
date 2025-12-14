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
    // ✅ THÊM 2 TRƯỜNG NÀY ĐỂ SHOW TRONG MODAL CHI TIẾT
    private BigDecimal personalIncomeTax;
    private BigDecimal insuranceDeduction;
    private List<PayrollItemResponseDTO> items;
}
