package com.project.hrms.dto;

import com.project.hrms.model.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveRequestDTO {
    @NotNull(message = "Trạng thái phê duyệt không được để trống")
    private RequestStatus status; // Approved hoặc Rejected
}