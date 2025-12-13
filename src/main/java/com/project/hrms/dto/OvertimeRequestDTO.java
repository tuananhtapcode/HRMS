package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hrms.model.enums.RequestStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OvertimeRequestDTO {
    private Long employeeId;

    @NotNull(message = "Ngày làm thêm không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private String reason;
}
