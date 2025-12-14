package com.project.hrms.response;

import com.project.hrms.model.AttendanceLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceLogResponse {
    private Long id;
    private LocalDateTime time;
    private String source; // App, Fingerprint...

    // Thông tin nhân viên (đã làm phẳng)
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;

    // Hàm tiện ích để convert từ Entity sang Response
    public static AttendanceLogResponse fromEntity(AttendanceLog log) {
        return AttendanceLogResponse.builder()
                .id(log.getId())
                .time(log.getTime())
                .source(log.getSource())
                .employeeId(log.getEmployee().getEmployeeId())
                .employeeCode(log.getEmployee().getEmployeeCode())
                .employeeName(log.getEmployee().getFullName())
                .departmentName(log.getEmployee().getDepartment() != null
                        ? log.getEmployee().getDepartment().getName()
                        : "N/A")
                .build();
    }
}