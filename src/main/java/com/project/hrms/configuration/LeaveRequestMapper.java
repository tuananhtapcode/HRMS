package com.project.hrms.configuration;

import com.project.hrms.dto.LeaveRequestDTO;
import com.project.hrms.model.LeaveRequest;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.response.LeaveRequestResponse;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMapper {
    //Mapper tính totalDays khi tạo entity.
    //Chuyển từ DTO sang entity.
    public LeaveRequest toEntity(LeaveRequestDTO dto) {
        LeaveRequest entity = LeaveRequest.builder()
                .employeeId(dto.getEmployeeId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .leaveType(dto.getLeaveType())
                .status(RequestStatus.PENDING)
                .reason(dto.getReason())
                .build();
        return entity;
    }

    //Chuyển từ entity → response DTO.
    public LeaveRequestResponse toResponse(LeaveRequest entity) {
        LeaveRequestResponse resp = new LeaveRequestResponse();
        resp.setLeaveRequestId(entity.getLeaveRequestId());
        resp.setEmployeeId(entity.getEmployeeId());
        resp.setAccountApproverId(entity.getAccountApproverId());
        resp.setStartDate(entity.getStartDate());
        resp.setEndDate(entity.getEndDate());
        resp.setTotalDays(entity.getTotalDays());
        resp.setLeaveType(entity.getLeaveType().name());
        resp.setReason(entity.getReason());
        resp.setStatus(entity.getStatus().name());
        resp.setApprovedAt(entity.getApprovedAt());
        resp.setApprovedNote(entity.getApprovedNote());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        return resp;
    }
}
