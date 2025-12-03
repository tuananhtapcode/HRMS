package com.project.hrms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimesheetSummaryDTO {
    // Thông tin nhân viên
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String jobPosition;

    // Các cột dữ liệu tổng hợp
    private Double standardWorkDays; // Số công chuẩn (VD: 22 hoặc 26 ngày)
    private Double actualWorkDays;   // Số công đi làm thực tế (Ngày thường)

    private Double paidLeaveDays;    // Số công nghỉ có phép (hưởng lương)
    private Double unpaidLeaveDays;  // Số công nghỉ không lương

    private Double overtimeHours;    // Tổng giờ làm thêm (OT)

    private Integer totalLateMinutes; // Tổng số phút đi muộn

    private Double totalPayableDays; // Tổng công hưởng lương (= Đi làm + Nghỉ phép)
}