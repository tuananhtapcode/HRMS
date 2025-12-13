package com.project.hrms.service;

import com.project.hrms.configuration.OvertimeRequestMapper;
import com.project.hrms.dto.OvertimeRequestDTO;
import com.project.hrms.dto.RequestApproveDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.AttendanceRecord;
import com.project.hrms.model.AuditLog;
import com.project.hrms.model.Employee;
import com.project.hrms.model.OvertimeRequest;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.repository.*;
import com.project.hrms.response.OvertimeRequestResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OvertimeRequestService implements IOvertimeRequestService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final AuditLogRepository auditRepo;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final OvertimeRequestMapper overtimeRequestMapper;
    private final AuthService authService;
    private final AttendanceRecordService attendanceService;
    private final EmployeeService employeeService;


    // Business constants
    private static final double MAX_OT_HOURS_PER_DAY = 8.0;
    private static final int MAX_OT_MINUTES_PER_DAY = (int) (MAX_OT_HOURS_PER_DAY * 60);

    /* ----------------------- Helpers ------------------------ */

    // Tính giờ theo start/end, làm tròn 2 chữ số
    private double computeHours(LocalTime start, LocalTime end) {
        long seconds = Duration.between(start, end).getSeconds();
        double hours = seconds / 3600.0;
        return Math.round(hours * 100.0) / 100.0;
    }

    // Ghi audit đơn giản
    private void saveAudit(Long requestId, AuditLog.AuditAction action, Long performedBy, String note) {
        // Lấy tên nhân viên để lưu snapshot (để hiển thị lịch sử chính xác kể cả khi nhân viên bị xóa)
        String actorName = "Unknown";
        if (performedBy != null) {
            // Cách 1: Query nhẹ để lấy tên (nếu chấp nhận thêm 1 query)
            actorName = employeeRepository.findById(performedBy)
                    .map(Employee::getFullName)
                    .orElse("Unknown ID: " + performedBy);
        }

        AuditLog a = AuditLog.builder()
                .entityName("OvertimeRequest")
                .entityId(requestId)
                .action(action)
                .performedBy(performedBy)
                .performedByName(actorName) // Lưu luôn tên vào DB
                .note(note)
                .build();
        auditRepo.save(a);
    }

    // Kiểm start < end, giờ > 0
    public void validateTimeRange(LocalTime start, LocalTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("startTime/endTime bắt buộc");
        if (!start.isBefore(end)) throw new IllegalArgumentException("startTime phải nhỏ hơn endTime");
    }

    /**
     * Kiểm trùng giờ: dùng repo query tìm các OT đang PENDING/APPROVED có overlap.
     * excludeId: nếu update muốn loại trừ chính request hiện tại.
     */
    public void checkOverlap(OvertimeRequestRepository repo, Long employeeId,
                             LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        List<RequestStatus> statuses = Arrays.asList(RequestStatus.PENDING, RequestStatus.APPROVED);
        var overlaps = repo.findOverlapping(employeeId, date, start, end, statuses);
        if (excludeId != null) {
            overlaps.removeIf(o -> o.getOvertimeRequestId().equals(excludeId));
        }
        if (!overlaps.isEmpty())
            throw new IllegalStateException("Thời gian OT trùng với OT khác đang PENDING/APPROVED");
    }

    /* ----------------------- API methods -------------------- */

    @Override
    @Transactional
    public OvertimeRequestResponse createRequest(OvertimeRequestDTO dto) {

        Long currentUserId = authService.getCurrentUserId();
        String role  = authService.getCurrentRole();

        Long employeeId;

        // Nếu nhân viên tự tạo: dto.employeeId = null → dùng id của current user
//        if (dto.getEmployeeId() == null) {employeeId = currentUserId;}
//        // Nếu quản lý tạo hộ: lấy employeeId từ DTO
//        else {employeeId = dto.getEmployeeId();}

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
            default -> throw new IllegalStateException("Không có quyền tạo yêu cầu OT");
        }

        Employee employee = employeeService.getById(employeeId);

        if (employee.getEmploymentType() != Employee.EmploymentType.FULLTIME) {
            throw new IllegalStateException("Chỉ nhân viên full-time được tạo yêu cầu OT");
        }

        LocalDate date = dto.getDate();
        LocalTime start = dto.getStartTime();
        LocalTime end = dto.getEndTime();

        validateTimeRange(start, end);
        double hours = computeHours(start, end);

        checkOverlap(overtimeRequestRepository, employeeId, date, start, end, null);

        int approvedMinutesToday = attendanceService.getOvertimeMinutes(employeeId, date);
        int requestedMin = (int) Math.round(hours * 60);

        if (approvedMinutesToday + requestedMin > MAX_OT_MINUTES_PER_DAY) {
            throw new IllegalStateException("Tổng OT đã duyệt trong ngày vượt quá 8 giờ");
        }

        // Tạo entity
        OvertimeRequest ent = overtimeRequestMapper.toEntity(dto, hours);
        ent.setEmployeeId(employeeId); // Đảm bảo đúng employee

        OvertimeRequest saved = overtimeRequestRepository.save(ent);

        saveAudit(saved.getOvertimeRequestId(), AuditLog.AuditAction.CREATE, currentUserId,
                "Tạo yêu cầu OT");

        return overtimeRequestMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public OvertimeRequestResponse updateRequest(Long requestId, OvertimeRequestDTO dto) {

        Long currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        OvertimeRequest req = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu OT"));

        // =========================
        // CHECK QUYỀN SỬA
        // =========================
        switch (role) {
            case "ROLE_EMPLOYEE" -> {
                if (!req.getEmployeeId().equals(currentUserId))
                    throw new IllegalStateException("Không được sửa yêu cầu OT của người khác");

                if (req.getStatus() != RequestStatus.PENDING)
                    throw new IllegalStateException("Chỉ được sửa khi yêu cầu đang ở trạng thái PENDING");
            }

            case "ROLE_ADMIN", "ROLE_MANAGER" -> {
                if (req.getStatus() != RequestStatus.PENDING)
                    throw new IllegalStateException("Chỉ đơn PENDING mới được update");
            }

            default -> throw new IllegalStateException("Không có quyền update yêu cầu OT");
        }

        // 4) Validate thời gian
        validateTimeRange(dto.getStartTime(), dto.getEndTime());
        double hours = computeHours(dto.getStartTime(), dto.getEndTime());
        int requestedMin = (int) Math.round(hours * 60);

        // 5) Check overlap (loại trừ chính đơn này)
        checkOverlap(
                overtimeRequestRepository,
                req.getEmployeeId(),
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime(),
                requestId
        );

        // 6) Check giới hạn OT trong ngày
        int approvedMinutesToday = attendanceService.getOvertimeMinutes(req.getEmployeeId(), dto.getDate());

        // Vì đơn đang PENDING nên không nằm trong approvedMinutesToday
        if (approvedMinutesToday + requestedMin > MAX_OT_MINUTES_PER_DAY) {
            throw new IllegalStateException("Yêu cầu sẽ vượt giới hạn OT trong ngày");
        }

        // 7) Áp dụng update
        req.setDate(dto.getDate());
        req.setStartTime(dto.getStartTime());
        req.setEndTime(dto.getEndTime());
        req.setReason(dto.getReason());
        req.setTotalHours(hours);

        OvertimeRequest saved = overtimeRequestRepository.save(req);
        saveAudit(requestId, AuditLog.AuditAction.UPDATE, currentUserId,
                "Cập nhật yêu cầu OT");
        return overtimeRequestMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public OvertimeRequestResponse approveRequest(Long requestId, RequestApproveDTO dto) {

        Long approverId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_MANAGER")) {
            throw new IllegalStateException("Bạn không có quyền duyệt yêu cầu OT");
        }

        OvertimeRequest req = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu OT"));

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể duyệt yêu cầu đang PENDING");
        }

        Long employeeId = req.getEmployeeId();
        LocalDate date = req.getDate();

        // 5) Tính số phút yêu cầu
        int minutes = (int) Math.round(req.getTotalHours() * 60);

        // BẮT BUỘC có attendance + shift
        attendanceRecordRepository
                .findByEmployee_EmployeeIdAndAttendanceDate(employeeId, date)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Nhân viên chưa có ca làm việc trong ngày này, không thể duyệt OT"
                        )
                );

        // Cộng OT
        int added = attendanceService.addOvertimeMinutes(employeeId, date, minutes);

        if (added <= 0) {
            throw new IllegalStateException("Không thể cộng OT (đã đạt giới hạn ngày)");
        }

        // Cập nhật trạng thái
        req.setStatus(RequestStatus.APPROVED);
        req.setAccountApproverId(approverId);
        req.setApprovedAt(LocalDateTime.now());
        req.setApprovedNote(dto.getManagerNote());
        OvertimeRequest saved =  overtimeRequestRepository.save(req);

        // 11) Audit
        saveAudit(
                saved.getOvertimeRequestId(),
                AuditLog.AuditAction.APPROVE,
                approverId,
                String.format("Duyệt, đã cộng %d phút vào attendance", added)
        );

        return overtimeRequestMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public OvertimeRequestResponse rejectRequest(Long requestId, RequestApproveDTO dto) {

        Long approverId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_MANAGER")) {
            throw new IllegalStateException("Bạn không có quyền từ chối yêu cầu OT");
        }

        OvertimeRequest req = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu OT"));

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể từ chối yêu cầu đang ở trạng thái PENDING");
        }

        // 5) Cập nhật trạng thái
        req.setStatus(RequestStatus.REJECTED);
        req.setAccountApproverId(approverId);
        req.setApprovedAt(LocalDateTime.now()); // hoặc decisionAt nếu bạn đổi tên
        req.setApprovedNote(dto.getManagerNote());
        overtimeRequestRepository.save(req);

        OvertimeRequest saved = overtimeRequestRepository.save(req);

        saveAudit(
                saved.getOvertimeRequestId(),
                AuditLog.AuditAction.REJECT,
                approverId,
                "Từ chối yêu cầu OT"
        );

        return overtimeRequestMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public OvertimeRequestResponse cancelRequest(Long id) {

        Long currentUserId = authService.getCurrentUserId();
        String role = authService.getCurrentRole();

        OvertimeRequest req = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu OT"));

        boolean isOwner = req.getEmployeeId().equals(currentUserId);
        boolean isAdminOrManager =
                role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER");

        // Rule
        if (isOwner) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new IllegalStateException("Nhân viên chỉ được huỷ đơn PENDING");
            }
        } else if (!isAdminOrManager) {
            throw new IllegalStateException("Không có quyền huỷ yêu cầu OT");
        }

        // Nếu đã APPROVED → rollback OT
        if (req.getStatus() == RequestStatus.APPROVED) {
            int minutes = (int) Math.round(req.getTotalHours() * 60);
            attendanceService.subtractOvertimeMinutes(
                    req.getEmployeeId(),
                    req.getDate(),
                    minutes
            );
        }

        req.setStatus(RequestStatus.CANCELLED);
        req.setApprovedAt(LocalDateTime.now());
        req.setAccountApproverId(currentUserId);

        OvertimeRequest saved = overtimeRequestRepository.save(req);

        saveAudit(
                saved.getOvertimeRequestId(),
                AuditLog.AuditAction.CANCEL,
                currentUserId,
                "Huỷ yêu cầu OT"
        );

        return overtimeRequestMapper.toResponse(saved);
    }



    @Override
    public Page<OvertimeRequestResponse> listAll(RequestStatus status, Pageable pageable) {
        Page<OvertimeRequest> page;
        if (status != null) {
            page = overtimeRequestRepository.findAll(Example.of(OvertimeRequest.builder().status(status).build()), pageable);
        } else {
            page = overtimeRequestRepository.findAll(pageable);
        }
        return page.map(overtimeRequestMapper::toResponse);
    }

    @Override
    public Page<OvertimeRequestResponse> getMyRequests(Pageable pageable) {

        Long currentId = authService.getCurrentUserId();

        Page<OvertimeRequest> page = overtimeRequestRepository
                .findByEmployeeId(currentId, pageable);

        return page.map(overtimeRequestMapper::toResponse);
    }



    @Override
    public OvertimeRequestResponse getById(Long id) {
        OvertimeRequest e = overtimeRequestRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Không tìm thấy OT"));
        return overtimeRequestMapper.toResponse(e);
    }

    @Override
    public Page<OvertimeRequestResponse> getEmployeeRequest(Long employeeId, RequestStatus status, Pageable pageable) {
        Page<OvertimeRequest> page;
        if (employeeId != null) {
            page = overtimeRequestRepository.findAll(Example.of(OvertimeRequest.builder().employeeId(employeeId).build()), pageable);
        } else {
            page = overtimeRequestRepository.findAll(pageable);
        }
        return page.map(overtimeRequestMapper::toResponse);
    }

    @Override
    public long countAllRequests() {
        return overtimeRequestRepository.count();
    }

    @Override
    public long countRequestsByStatus(RequestStatus status) {
        return (status == null)
                ? overtimeRequestRepository.count()
                : overtimeRequestRepository.countByStatus(status);
    }

    @Override
    public long countRequestsByEmployee(Long employeeId, RequestStatus status) {
        return (status == null)
                ? overtimeRequestRepository.countByEmployeeId(employeeId)
                : overtimeRequestRepository.countByEmployeeIdAndStatus(employeeId, status);
    }

    @Override
    public long countPending() {
        return overtimeRequestRepository.countByStatus(RequestStatus.PENDING);
    }

    @Override
    public long countApproved() {
        return overtimeRequestRepository.countByStatus(RequestStatus.APPROVED);
    }

    @Override
    public long countRejected() {
        return overtimeRequestRepository.countByStatus(RequestStatus.REJECTED);
    }

    @Override
    public long countCancelled() {
        return overtimeRequestRepository.countByStatus(RequestStatus.CANCELLED);
    }

    /* -------- Total minutes (dashboard) ---------- */

    @Override
    public int getTotalOvertimeMinutesAll() {
        return overtimeRequestRepository.sumApprovedMinutesAll();
    }

    @Override
    public int getTotalOvertimeMinutesByEmployee(Long employeeId) {
        return overtimeRequestRepository.sumApprovedMinutesByEmployee(employeeId);
    }

    /* -------- Monthly stats ---------- */

    @Override
    public Map<Integer, Integer> getMonthlyStats(int year) {
        var rows = overtimeRequestRepository.getMonthlyApprovedMinutes(year);

        Map<Integer, Integer> stats = new HashMap<>();
        for (Object[] row : rows) {
            Integer month = ((Number) row[0]).intValue();
            Integer minutes = ((Number) row[1]).intValue();
            stats.put(month, minutes);
        }

        // Đảm bảo từ tháng 1–12 luôn có key (nếu tháng nào = 0 thì trả về 0)
        for (int i = 1; i <= 12; i++) {
            stats.putIfAbsent(i, 0);
        }

        return stats;
    }

    /* ----------------- Utility: resolve current user id from SecurityContext ------------- */

    private Long resolveCurrentUserId() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new IllegalStateException("Không xác định được user hiện tại (chưa login)");
        }

        String username = auth.getName(); // Lấy username/email từ token/session

        // Tìm account/employee id dựa trên username
        return accountRepository.findByUsername(username)
                .map(account -> account.getEmployee().getEmployeeId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy thông tin nhân viên cho user: " + username));
    }
}
