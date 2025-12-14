package com.project.hrms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlySummaryDTO {
    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private int totalWorkDays;      // Tổng ngày công
    private int totalLateMinutes;   // Tổng phút trễ
    private int totalOvertimeMinutes; // Tổng phút OT
    private int totalLeaveDays;     // Tổng ngày nghỉ phép
}