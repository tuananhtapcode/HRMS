package com.project.hrms.response;

import com.project.hrms.model.AttendanceRecord;
import com.project.hrms.model.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private String employeeName;
    private String shiftName;     // Tên ca (VD: Ca Hành Chính)
    private LocalDate date;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private AttendanceStatus status;
    private Integer lateMinutes;
    private Integer totalWorkMinutes;

    // Hàm tiện ích để convert từ Entity sang Response (Giống AccountResponse)
    public static AttendanceResponse fromEntity(AttendanceRecord record) {
        return AttendanceResponse.builder()
                .id(record.getAttendanceRecordId())
                .employeeName(record.getEmployee().getFullName())
                .shiftName(record.getShift().getName())
                .date(record.getAttendanceDate())
                .checkIn(record.getCheckInTime())
                .checkOut(record.getCheckOutTime())
                .status(record.getStatus())
                .lateMinutes(record.getLateMinutes())
                .totalWorkMinutes(record.getTotalWorkMinutes())
                .build();
    }
}