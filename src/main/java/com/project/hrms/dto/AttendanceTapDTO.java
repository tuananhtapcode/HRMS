package com.project.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceTapDTO {
    // Có thể thêm tọa độ GPS, IP wifi nếu cần sau này
    private String source; // "APP", "WEB"
}