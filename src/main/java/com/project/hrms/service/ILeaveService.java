package com.project.hrms.service;

import com.project.hrms.dto.ApproveRequestDTO;
import com.project.hrms.dto.LeaveRequestDTO;
import com.project.hrms.model.enums.RequestStatus;

import java.util.List;

public interface ILeaveService {
    // Nhân viên tạo đơn
    LeaveRequestDTO createLeaveRequest(LeaveRequestDTO dto, String username);

    // Nhân viên xem lịch sử đơn của mình
    List<LeaveRequestDTO> getMyLeaveRequests(String username);

    // Quản lý duyệt đơn
    LeaveRequestDTO approveLeaveRequest(Long id, ApproveRequestDTO dto, String approverUsername);
    List<LeaveRequestDTO> getAllLeaveRequests(RequestStatus status); // Trong ILeaveService
//    List<OvertimeRequestDTO> getAllOvertimeRequests(RequestStatus status); // Trong IOvertimeService
}