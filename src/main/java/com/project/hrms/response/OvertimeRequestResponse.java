package com.project.hrms.response;

import com.project.hrms.model.enums.RequestStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
public class OvertimeRequestResponse {
    private Long id;
    private Long employeeId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double totalHours;
    private String reason;
    private RequestStatus status;
    private Long accountApproverId;
    private LocalDateTime approvedAt;
    private String approvedNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
