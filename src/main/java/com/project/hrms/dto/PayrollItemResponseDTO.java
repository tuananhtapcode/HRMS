package com.project.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PayrollItemResponseDTO {
    private Long salaryComponentId;
    private String code;
    private String name;
    private String type; // earning | deduction
    private BigDecimal amount;
}
