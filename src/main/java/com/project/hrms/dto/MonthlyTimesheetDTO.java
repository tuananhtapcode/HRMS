package com.project.hrms.dto;

import com.project.hrms.model.MonthlyTimesheet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyTimesheetDTO {
    private Long id;
    private int month;
    private int year;

    // Chỉ lấy thông tin cơ bản của Employee
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String departmentName;

    // Các chỉ số quan trọng
    private Double standardWorkDays;
    private Double actualWorkDays;
    private Double paidLeaveDays;
    private Double unpaidLeaveDays;
    private Double totalOtHours;
    private Double otWeekdayHours;
    private Double otWeekendHours;
    private Double otHolidayHours;
    private Integer lateCount;
    private String status;

    // Helper mapper
    public static MonthlyTimesheetDTO fromEntity(MonthlyTimesheet entity) {
        return MonthlyTimesheetDTO.builder()
                .id(entity.getId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .employeeId(entity.getEmployee().getEmployeeId())
                .employeeName(entity.getEmployee().getFullName())
                .employeeCode(entity.getEmployee().getEmployeeCode())
                .departmentName(entity.getEmployee().getDepartment() != null ? entity.getEmployee().getDepartment().getName() : "")
                .standardWorkDays(entity.getStandardWorkDays())
                .actualWorkDays(entity.getActualWorkDays())
                .paidLeaveDays(entity.getPaidLeaveDays())
                .unpaidLeaveDays(entity.getUnpaidLeaveDays())
                .totalOtHours(entity.getTotalOtHours())
                .otWeekdayHours(entity.getOtWeekdayHours())
                .otWeekendHours(entity.getOtWeekendHours())
                .otHolidayHours(entity.getOtHolidayHours())
                .lateCount(entity.getLateCount())
                .status(entity.getStatus())
                .build();
    }
}