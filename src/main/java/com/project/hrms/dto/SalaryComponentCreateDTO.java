package com.project.hrms.dto;

import com.project.hrms.model.enums.SalaryComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalaryComponentCreateDTO {
    @NotBlank private String code;
    @NotBlank private String name;
    @NotNull private SalaryComponentType type; // earning/deduction
    private String description;
    private Boolean isActive = true;
}
