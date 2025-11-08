package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPositionDTO {

    private String code;

    private String name;

    private String description;
    private String level;

//    @JsonProperty("min_salary")
    private BigDecimal minSalary;

//    @JsonProperty("max_salary")
    private BigDecimal maxSalary;
}
