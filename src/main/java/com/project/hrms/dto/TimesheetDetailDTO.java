package com.project.hrms.dto;

import com.project.hrms.model.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public class TimesheetDetailDTO {

    @Data
    @Builder
    public static class Response {
        private Long employeeId;
        private String employeeCode;
        private String fullName;
        // Danh sách công từng ngày trong tháng
        private List<DailyItem> dailyRecords;
    }

    @Data
    @Builder
    public static class DailyItem {
        private LocalDate date;
        private AttendanceStatus status; // PRESENT, LATE, ABSENT...
        private Double hoursWorked;      // Số giờ làm (để hiển thị nếu cần)
        private Double hoursOvertime;    // Số giờ OT
    }
}