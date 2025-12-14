package com.project.hrms.dto;

import lombok.Data;

@Data
public class PayrollCalculateRequestDTO {
    private Long employeeId;
    private Long payrollPeriodId;
    private Integer month;
    private Integer year;
}
