package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hrms.model.enums.RequestStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class LeaveRequestDTO {

    private Long id; // Output only

    @NotBlank(message = "Loại nghỉ không được để trống")
    private String leaveType;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String reason;

    private RequestStatus status; // Output only
    private String approverName;  // Output only
}