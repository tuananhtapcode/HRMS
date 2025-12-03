package com.project.hrms.service;

import com.project.hrms.dto.ApproveRequestDTO;
import com.project.hrms.dto.OvertimeRequestDTO;
import com.project.hrms.model.enums.RequestStatus;

import java.util.List;

public interface IOvertimeService {
    OvertimeRequestDTO createOvertimeRequest(OvertimeRequestDTO dto, String username);
    List<OvertimeRequestDTO> getMyOvertimeRequests(String username);
    OvertimeRequestDTO approveOvertimeRequest(Long id, ApproveRequestDTO dto, String approverUsername);
//    List<LeaveRequestDTO> getAllLeaveRequests(RequestStatus status); // Trong ILeaveService
    List<OvertimeRequestDTO> getAllOvertimeRequests(RequestStatus status); // Trong IOvertimeService
}