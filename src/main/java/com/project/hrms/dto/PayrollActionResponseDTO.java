package com.project.hrms.dto;

import com.project.hrms.model.enums.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PayrollActionResponseDTO {
    private Long payrollPeriodId;
    private PayrollStatus status;
    private int affectedCount;
    private LocalDateTime at;
}
