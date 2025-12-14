package com.project.hrms.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CurrentPeriodDTO {
    private Long id;
    private String name;
    private String status;
    private LocalDate startDate;
    private LocalDate paymentDate;
    private String approver;
}
