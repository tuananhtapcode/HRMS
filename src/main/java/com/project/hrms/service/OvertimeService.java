package com.project.hrms.service;

import com.project.hrms.dto.ApproveRequestDTO;
import com.project.hrms.dto.OvertimeRequestDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Employee;
import com.project.hrms.model.OvertimeRequest;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.OvertimeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OvertimeService implements IOvertimeService {

    private final OvertimeRequestRepository overtimeRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public OvertimeRequestDTO createOvertimeRequest(OvertimeRequestDTO dto, String username) {
        // 1. Lấy thông tin nhân viên
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Account not found"));
        Employee employee = account.getEmployee();
        if (employee == null) throw new DataNotFoundException("Employee profile not found");

        // 2. Validate (Ví dụ: Không được đăng ký OT cho quá khứ quá xa, hoặc hours <= 0)
        // (Validation @DecimalMin đã làm ở DTO, ở đây có thể check logic nghiệp vụ khác)

        // 3. Tạo đơn
        OvertimeRequest request = new OvertimeRequest();
        request.setEmployee(employee);
        request.setDate(dto.getDate());
        request.setHours(dto.getHours());
        request.setReason(dto.getReason());
        request.setStatus(RequestStatus.Pending); // Mặc định chờ duyệt

        OvertimeRequest saved = overtimeRepository.save(request);
        return mapToDTO(saved);
    }

    @Override
    public List<OvertimeRequestDTO> getMyOvertimeRequests(String username) {
        Account account = accountRepository.findByUsername(username).orElseThrow();
        Long empId = account.getEmployee().getEmployeeId();

        return overtimeRepository.findByEmployee_EmployeeIdOrderByCreatedAtDesc(empId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OvertimeRequestDTO approveOvertimeRequest(Long id, ApproveRequestDTO dto, String approverUsername) {
        OvertimeRequest request = overtimeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Overtime request not found"));

        if (request.getStatus() != RequestStatus.Pending) {
            throw new InvalidParamException("Chỉ có thể duyệt đơn đang ở trạng thái Chờ duyệt");
        }

        // Cập nhật trạng thái
        request.setStatus(dto.getStatus());
        // (Lưu ý: Bảng overtime_request trong file SQL cũ KHÔNG có cột account_approver_id
        // nên ta không lưu người duyệt vào DB ở bảng này, trừ khi bạn thêm cột đó vào)

        OvertimeRequest saved = overtimeRepository.save(request);
        return mapToDTO(saved);
    }
    @Override
    public List<OvertimeRequestDTO> getAllOvertimeRequests(RequestStatus status) {
        List<OvertimeRequest> requests;

        if (status != null) {
            requests = overtimeRepository.findByStatus(status);
        } else {
            requests = overtimeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        return requests.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Helper map từ Entity -> DTO
    private OvertimeRequestDTO mapToDTO(OvertimeRequest entity) {
        return OvertimeRequestDTO.builder()
                .id(entity.getOvertimeRequestId())
                .date(entity.getDate())
                .hours(entity.getHours())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .build();
    }
}