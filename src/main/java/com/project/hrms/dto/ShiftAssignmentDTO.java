// src/main/java/com/project/hrms/dto/ShiftAssignmentDTO.java
package com.project.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ShiftAssignmentDTO {

    private Long shiftAssignmentId; // Dùng cho output

    @NotNull(message = "ID Nhân viên không được để trống")
    private Long employeeId;

    @NotNull(message = "ID Ca làm việc không được để trống")
    private Long shiftId;

    @NotNull(message = "Ngày phân ca không được để trống")
    private LocalDate assignmentDate;

    private Boolean isApproved = true; // Mặc định là đã duyệt

    private String note;
}