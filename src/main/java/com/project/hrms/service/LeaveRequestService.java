package com.project.hrms.service;

import com.project.hrms.configuration.LeaveRequestMapper;
import com.project.hrms.dto.LeaveRequestDTO;
import com.project.hrms.dto.RequestApproveDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.AuditLog;
import com.project.hrms.model.Employee;
import com.project.hrms.model.LeaveRequest;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.repository.AuditLogRepository;
import com.project.hrms.repository.LeaveRequestRepository;
import com.project.hrms.repository.WorkdayRepository;
import com.project.hrms.response.LeaveRequestResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LeaveRequestService implements ILeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final AuthService authService;
    private final EmployeeService employeeService;
    private final WorkdayRepository workdayRepository;
    private final AuditLogRepository auditRepo;

    private static final int MAX_LEAVE_DAYS = 12;
//        * ---------------- Helper ----------------- *

    // Tính số ngày nghỉ giữa start & end, loại trừ ngày không làm việc
    private int calculateLeaveDays(Employee employee, LocalDate start, LocalDate end) {
        int days = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            // FULLTIME: tính tất cả ngày làm việc trong tuần
            // Giả sử employee có lịch làm việc tuần (DayOfWeek), ở đây tạm: Mon-Fri
            if (employee.getEmploymentType() == Employee.EmploymentType.FULLTIME) {
                DayOfWeek dow = d.getDayOfWeek();
                if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                    days++;
                }
            }
        }
        return days;
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể tạo đơn nghỉ cho ngày đã qua");
        }
    }

    private void validateNoOverlap(Long employeeId, LocalDate start, LocalDate end, Long excludeId) {
        boolean exists = leaveRequestRepository.existsOverlap(employeeId, start, end, excludeId);
        if (exists) {
            throw new IllegalArgumentException("Khoảng thời gian này đã có đơn nghỉ khác");
        }
    }

    private void saveAudit(Long requestId, AuditLog.AuditAction action, Long performedBy, String note) {
        String actorName = "Unknown";
        if (performedBy != null) {
            actorName = employeeService.getById(performedBy).getFullName();
        }

        AuditLog audit = AuditLog.builder()
                .entityName("LeaveRequest")
                .entityId(requestId)
                .action(action)
                .performedBy(performedBy)
                .performedByName(actorName)
                .note(note)
                .build();

        // Lưu audit vào repo tương tự OvertimeRequest
         auditRepo.save(audit);
    }

    /* ---------------- API ------------------ */
    @Override
    @Transactional
    public LeaveRequestResponse createRequest(LeaveRequestDTO dto) {
        Long currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        Long employeeId;

        // XÁC ĐỊNH employeeId ĐÚNG LOGIC
        // ============================
        switch (role) {
            // Nhân viên → tự tạo cho chính mình (dto.employeeId bị bỏ qua)
            case "ROLE_EMPLOYEE" -> employeeId = currentUserId;

            // Admin/Manager → có thể tạo hộ
            case "ROLE_ADMIN", "ROLE_MANAGER" -> {
                if (dto.getEmployeeId() == null) {
                    throw new IllegalArgumentException("Admin/Manager phải cung cấp employeeId khi tạo đơn OT");
                }
                employeeId = dto.getEmployeeId();
            }
            default -> throw new IllegalStateException("Không có quyền tạo đơn xin nghỉ");
        }

        Employee employee = employeeService.getById(employeeId);
        if (employee.getEmploymentType() != Employee.EmploymentType.FULLTIME) {
            throw new IllegalStateException("Chỉ nhân viên FULLTIME mới cần tạo đơn nghỉ");
        }

        validateDateRange(dto.getStartDate(), dto.getEndDate());
        validateNoOverlap(employeeId, dto.getStartDate(), dto.getEndDate(), null);

        int totalDays = calculateLeaveDays(employee, dto.getStartDate(), dto.getEndDate());
        if (totalDays > MAX_LEAVE_DAYS) {
            throw new IllegalStateException("Số ngày nghỉ vượt quá hạn mức " + MAX_LEAVE_DAYS);
        }

        LeaveRequest ent = leaveRequestMapper.toEntity(dto);
        ent.setEmployeeId(employeeId);
        ent.setTotalDays(totalDays);

        LeaveRequest saved = leaveRequestRepository.save(ent);
        saveAudit(saved.getLeaveRequestId(), AuditLog.AuditAction.CREATE, currentUserId, "Tạo đơn nghỉ");

        return leaveRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse updateRequest(Long id, LeaveRequestDTO dto) {

        Long currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn nghỉ"));

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new InvalidParamException("Chỉ đơn PENDING mới được sửa");
        }

        if (role.equals("ROLE_EMPLOYEE") && !req.getEmployeeId().equals(currentUserId)) {
            throw new InvalidParamException("Không được sửa đơn của người khác");
        }

        validateDateRange(dto.getStartDate(), dto.getEndDate());
        validateNoOverlap(req.getEmployeeId(), dto.getStartDate(), dto.getEndDate(), req.getLeaveRequestId());

        Employee employee = employeeService.getById(req.getEmployeeId());
        int totalDays = calculateLeaveDays(employee, dto.getStartDate(), dto.getEndDate());

        if (totalDays > MAX_LEAVE_DAYS) {
            throw new IllegalStateException("Số ngày nghỉ vượt hạn mức " + MAX_LEAVE_DAYS);
        }

        req.setStartDate(dto.getStartDate());
        req.setEndDate(dto.getEndDate());
        req.setReason(dto.getReason());
        req.setTotalDays(totalDays);

        LeaveRequest saved = leaveRequestRepository.save(req);
        saveAudit(saved.getLeaveRequestId(), AuditLog.AuditAction.UPDATE, currentUserId, "Cập nhật đơn nghỉ");

        return leaveRequestMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public LeaveRequestResponse approveRequest(Long id, RequestApproveDTO dto) {

        Long approverId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_MANAGER")) {
            throw new IllegalStateException("Bạn không có quyền duyệt yêu cầu OT");
        }

        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn nghỉ"));


        if (req.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Chỉ đơn PENDING mới được approve");
        }

        req.setStatus(RequestStatus.APPROVED);
        req.setAccountApproverId(approverId);
        req.setApprovedAt(LocalDateTime.now());
        req.setApprovedNote(dto.getManagerNote());

        LeaveRequest saved = leaveRequestRepository.save(req);
        saveAudit(saved.getLeaveRequestId(), AuditLog.AuditAction.APPROVE, approverId, "Duyệt đơn nghỉ");

        return leaveRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse rejectRequest(Long id, RequestApproveDTO dto) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn nghỉ"));

        Long approverId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_MANAGER")) {
            throw new IllegalStateException("Bạn không có quyền từ chối yêu cầu OT");
        }

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Chỉ đơn PENDING mới được reject");
        }

        req.setStatus(RequestStatus.REJECTED);
        req.setAccountApproverId(approverId);
        req.setApprovedAt(LocalDateTime.now());
        req.setApprovedNote(dto.getManagerNote());

        LeaveRequest saved = leaveRequestRepository.save(req);
        saveAudit(saved.getLeaveRequestId(), AuditLog.AuditAction.REJECT, approverId, "Từ chối đơn nghỉ");

        return leaveRequestMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public LeaveRequestResponse cancelRequest(Long id) {

        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn nghỉ"));

        Long actorId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        boolean isOwner = req.getEmployeeId().equals(actorId);
        boolean isAdmin = role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER") || role.equals("ROLE_HR");

        if (req.getStartDate().isBefore(LocalDate.now())) {
            throw new InvalidParamException("Không thể hủy đơn đã bắt đầu");
        }

        if (isOwner && req.getStatus() != RequestStatus.PENDING) {
            throw new InvalidParamException("Nhân viên chỉ được hủy đơn PENDING");
        }

        if (!isOwner && !isAdmin) {
            throw new InvalidParamException("Không có quyền hủy đơn này");
        }

        RequestStatus oldStatus = req.getStatus();
        req.setStatus(RequestStatus.CANCELLED);

        if (isAdmin) {
            req.setApprovedAt(LocalDateTime.now());
            req.setAccountApproverId(actorId);
        }

        LeaveRequest saved = leaveRequestRepository.save(req);

        if (oldStatus == RequestStatus.APPROVED) {
            workdayRepository.deleteAll(
                    workdayRepository.findByEmployee_EmployeeIdAndDateBetween(
                                    req.getEmployeeId(),
                                    req.getStartDate(),
                                    req.getEndDate()
                            ).stream()
                            .filter(w -> Objects.equals(w.getLeaveRequestId(), req.getLeaveRequestId()))
                            .toList()
            );
        }

        saveAudit(id, AuditLog.AuditAction.CANCEL, actorId, "Cancel leave request. Old status=" + oldStatus);

        return leaveRequestMapper.toResponse(saved);
    }

    @Override
    public Page<LeaveRequestResponse> listAll(RequestStatus status, Pageable pageable) {
        Page<LeaveRequest> page;
        if (status != null) {
            page = leaveRequestRepository.findAll(Example.of(LeaveRequest.builder().status(status).build()), pageable);
        } else {
            page = leaveRequestRepository.findAll(pageable);
        }
        return page.map(leaveRequestMapper::toResponse);
    }

    @Override
    public Page<LeaveRequestResponse> getMyRequests(Pageable pageable) {
        Long currentId = authService.getCurrentUserId();
        Page<LeaveRequest> page = leaveRequestRepository.findByEmployeeId(currentId, pageable);
        return page.map(leaveRequestMapper::toResponse);
    }

    @Override
    public Page<LeaveRequestResponse> getEmployeeRequest(Long employeeId, RequestStatus status, Pageable pageable) {
        Page<LeaveRequest> page;
        if (employeeId != null) {
            page = leaveRequestRepository.findAll(Example.of(LeaveRequest.builder().employeeId(employeeId).build()), pageable);
        } else {
            page = leaveRequestRepository.findAll(pageable);
        }
        return page.map(leaveRequestMapper::toResponse);
    }

    @Override
    public LeaveRequestResponse getById(Long id) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn nghỉ"));
        return leaveRequestMapper.toResponse(req);
    }

    @Override
    public long countAllRequests() {
        return leaveRequestRepository.count();
    }

    @Override
    public long countRequestsByStatus(RequestStatus status) {
        return (status == null) ? leaveRequestRepository.count() : leaveRequestRepository.countByStatus(status);
    }

    @Override
    public long countRequestsByEmployee(Long employeeId, RequestStatus status) {
        return (status == null) ? leaveRequestRepository.countByEmployeeId(employeeId)
                : leaveRequestRepository.countByEmployeeIdAndStatus(employeeId, status);
    }

    @Override
    public long countPending() {
        return leaveRequestRepository.countByStatus(RequestStatus.PENDING);
    }

    @Override
    public long countApproved() {
        return leaveRequestRepository.countByStatus(RequestStatus.APPROVED);
    }

    @Override
    public long countRejected() {
        return leaveRequestRepository.countByStatus(RequestStatus.REJECTED);
    }

    @Override
    public long countCancelled() {
        return leaveRequestRepository.countByStatus(RequestStatus.CANCELLED);
    }

}
