package com.project.hrms.dto;

import com.project.hrms.model.enums.SalaryComponentType;
import lombok.Data;

@Data
public class SalaryComponentUpdateDTO {
    private String name;
    private SalaryComponentType type;
    private String description;
    private Boolean isActive;
}
