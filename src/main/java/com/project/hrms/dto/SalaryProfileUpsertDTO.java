package com.project.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryProfileUpsertDTO {
    @NotNull private Long salaryComponentId;
    @NotNull private BigDecimal amount;
    private String note;
}
