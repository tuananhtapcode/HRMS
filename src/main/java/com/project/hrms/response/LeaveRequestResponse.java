package com.project.hrms.response;

import com.project.hrms.model.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponse {
    private Long leaveRequestId;
    private Long employeeId;
    private Long accountApproverId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private String status;
    private LocalDateTime approvedAt;
    private String approvedNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
