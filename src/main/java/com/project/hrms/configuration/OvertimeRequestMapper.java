package com.project.hrms.configuration;

import com.project.hrms.dto.OvertimeRequestDTO;
import com.project.hrms.model.OvertimeRequest;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.response.OvertimeRequestResponse;
import org.springframework.stereotype.Component;

@Component
public class OvertimeRequestMapper {
    public OvertimeRequest toEntity(OvertimeRequestDTO dto, Double computedHours) {
        return OvertimeRequest.builder()
                .employeeId(dto.getEmployeeId())
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .totalHours(computedHours)
                .reason(dto.getReason())
                .status(RequestStatus.PENDING)
                .build();
    }

    public OvertimeRequestResponse toResponse(OvertimeRequest e) {
        OvertimeRequestResponse r = new OvertimeRequestResponse();
        r.setId(e.getOvertimeRequestId());
        r.setEmployeeId(e.getEmployeeId());
        r.setDate(e.getDate());
        r.setStartTime(e.getStartTime());
        r.setEndTime(e.getEndTime());
        r.setTotalHours(e.getTotalHours());
        r.setReason(e.getReason());
        r.setStatus(e.getStatus());
        r.setAccountApproverId(e.getAccountApproverId());
        r.setApprovedAt(e.getApprovedAt());
        r.setApprovedNote(e.getApprovedNote());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}

