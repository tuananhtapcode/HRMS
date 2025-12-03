package com.project.hrms.service;

import com.project.hrms.dto.ApproveRequestDTO;
import com.project.hrms.dto.LeaveRequestDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService implements ILeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO dto, String username) {
        // 1. Lấy nhân viên từ token
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Account not found"));
        Employee employee = account.getEmployee();
        if (employee == null) throw new DataNotFoundException("Employee profile not found");

        // 2. Validate ngày
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new InvalidParamException("Ngày kết thúc không được trước ngày bắt đầu");
        }

        // 3. Tạo đơn
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(dto.getLeaveType());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setReason(dto.getReason());
        request.setStatus(RequestStatus.Pending); // Mặc định chờ duyệt

        LeaveRequest saved = leaveRepository.save(request);
        return mapToDTO(saved);
    }

    @Override
    public List<LeaveRequestDTO> getMyLeaveRequests(String username) {
        Account account = accountRepository.findByUsername(username).orElseThrow();
        Long empId = account.getEmployee().getEmployeeId();

        return leaveRepository.findByEmployee_EmployeeIdOrderByCreatedAtDesc(empId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveRequestDTO approveLeaveRequest(Long id, ApproveRequestDTO dto, String approverUsername) {
        LeaveRequest request = leaveRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Leave request not found"));

        if (request.getStatus() != RequestStatus.Pending) {
            throw new InvalidParamException("Chỉ có thể duyệt đơn đang ở trạng thái Chờ duyệt");
        }

        // Lấy thông tin người duyệt
        Account approver = accountRepository.findByUsername(approverUsername).orElseThrow();

        request.setStatus(dto.getStatus());
        request.setApprover(approver); // Lưu người duyệt vào bảng leave_request

        // TODO: Logic trừ phép năm hoặc cập nhật bảng chấm công (attendance) sẽ nằm ở đây
        // Nhưng vì làm MVP đơn giản nên ta chỉ update trạng thái trước.

        return mapToDTO(leaveRepository.save(request));
    }
    @Override
    public List<LeaveRequestDTO> getAllLeaveRequests(RequestStatus status) {
        List<LeaveRequest> requests;

        if (status != null) {
            // Nếu có truyền status (VD: Pending), chỉ lấy đơn Pending
            requests = leaveRepository.findByStatus(status);
        } else {
            // Nếu không truyền, lấy tất cả
            requests = leaveRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        return requests.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Helper map
    private LeaveRequestDTO mapToDTO(LeaveRequest entity) {
        return LeaveRequestDTO.builder()
                .id(entity.getLeaveRequestId())
                .leaveType(entity.getLeaveType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .approverName(entity.getApprover() != null ? entity.getApprover().getUsername() : null)
                .build();
    }
}