package com.project.hrms.service;

import com.project.hrms.dto.RequestApproveDTO;
import com.project.hrms.dto.LeaveRequestDTO;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.response.LeaveRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ILeaveRequestService {

    LeaveRequestResponse createRequest(LeaveRequestDTO dto);

    LeaveRequestResponse updateRequest(Long id, LeaveRequestDTO dto);

    LeaveRequestResponse approveRequest(Long id, RequestApproveDTO dto);

    LeaveRequestResponse rejectRequest(Long id, RequestApproveDTO dto);

    LeaveRequestResponse cancelRequest(Long id);

    Page<LeaveRequestResponse> listAll(RequestStatus status, Pageable pageable);

    Page<LeaveRequestResponse> getMyRequests(Pageable pageable);

    Page<LeaveRequestResponse> getEmployeeRequest(Long employeeId, RequestStatus status, Pageable pageable);

    LeaveRequestResponse getById(Long id);

    long countAllRequests();
    long countRequestsByStatus(RequestStatus status);
    long countRequestsByEmployee(Long employeeId, RequestStatus status);

    long countPending();
    long countApproved();
    long countRejected();
    long countCancelled();
}
