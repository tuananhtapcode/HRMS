// src/main/java/com/project/hrms/dto/BulkAssignDTO.java
package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BulkAssignDTO {

    @JsonProperty("department_id")
    @NotNull(message = "ID Phòng ban không được để trống")
    private Long departmentId;

    @JsonProperty("shift_id")
    @NotNull(message = "ID Ca làm việc không được để trống")
    private Long shiftId;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private String note;
}