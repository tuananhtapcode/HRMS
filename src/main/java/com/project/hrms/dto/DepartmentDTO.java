package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO{

    private String code;

    private String name;

    private String description;

    @JsonProperty("manager_id")
    private Long managerId;
}