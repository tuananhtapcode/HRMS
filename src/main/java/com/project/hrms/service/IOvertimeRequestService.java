package com.project.hrms.service;

import com.project.hrms.dto.OvertimeRequestDTO;
import com.project.hrms.dto.RequestApproveDTO;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.response.OvertimeRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;


public interface IOvertimeRequestService {
    OvertimeRequestResponse createRequest(OvertimeRequestDTO dto);

    OvertimeRequestResponse updateRequest(Long id, OvertimeRequestDTO dto);

    OvertimeRequestResponse approveRequest(Long id, RequestApproveDTO dto);

    OvertimeRequestResponse rejectRequest(Long id, RequestApproveDTO dto);

    OvertimeRequestResponse cancelRequest(Long id);

    Page<OvertimeRequestResponse> getAll(RequestStatus status, Pageable pageable);

    Page<OvertimeRequestResponse> getMyRequests(Pageable pageable);

    Page<OvertimeRequestResponse> getEmployeeRequest(Long employeeId, RequestStatus status, Pageable pageable);

    OvertimeRequestResponse getById(Long id);

    long countAllRequests();

    long countRequestsByStatus(RequestStatus status);

    long countRequestsByEmployee(Long employeeId, RequestStatus status);

    long countPending();

    long countApproved();

    long countRejected();

    long countCancelled();

    int getTotalOvertimeMinutesAll();

    int getTotalOvertimeMinutesByEmployee(Long employeeId);

    Map<Integer, Integer> getMonthlyStats(int year);

    /* ================= ADMIN / MANAGER ================= */

    Page<OvertimeRequestResponse> getAllPending(Pageable pageable);

    Page<OvertimeRequestResponse> getAllApproved(Pageable pageable);

    Page<OvertimeRequestResponse> getAllRejected(Pageable pageable);

    /* ================= EMPLOYEE ================= */

    Page<OvertimeRequestResponse> getMyPending(Pageable pageable);

    Page<OvertimeRequestResponse> getMyApproved(Pageable pageable);

    Page<OvertimeRequestResponse> getMyRejected(Pageable pageable);
}

