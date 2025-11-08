package com.project.hrms.response;

import com.project.hrms.model.JobPosition;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class JobPositionResponse {
    private Long id;
    private String code;

    private String name;

    private String description;
    private String level;

    //    @JsonProperty("min_salary")
    private BigDecimal minSalary;

    //    @JsonProperty("max_salary")
    private BigDecimal maxSalary;

    public static JobPositionResponse fromJobPosition(JobPosition entity) {
        return JobPositionResponse.builder()
                .id(entity.getJobPositionId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .level(entity.getLevel())
                .minSalary(entity.getMinSalary())
                .maxSalary(entity.getMaxSalary())
                .build();
    }
}

