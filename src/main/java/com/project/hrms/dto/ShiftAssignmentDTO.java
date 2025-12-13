package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignmentDTO {

    private Long shiftAssignmentId;

    // --- Thông tin Core ---
    private Long employeeId;
    private Long shiftId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate assignmentDate;

    private Boolean isApproved;
    private String note;

    // --- MỞ RỘNG: Thông tin hiển thị (UI Friendly) ---
    private String employeeCode;   // Mới
    private String employeeName;   // Đã có
    private String jobPosition;    // Mới
    private String departmentName; // Mới

    // Thông tin ca (để vẽ thanh thời gian)
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String shiftCode;
}