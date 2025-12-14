package com.project.hrms.dto;

import com.project.hrms.model.enums.SalaryComponentType;
import lombok.Data;
import java.math.BigDecimal; // Nhớ import cái này

@Data
public class SalaryComponentUpdateDTO {
    private String name;
    private SalaryComponentType type;
    private String description;
    private Boolean isActive;

    // 👇 BỔ SUNG TRƯỜNG NÀY
    private BigDecimal amount;
}
