package com.project.hrms.service;

import com.project.hrms.dto.AttendanceTapDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidActionException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.AttendanceRecordRepository;
import com.project.hrms.repository.ShiftAssignmentRepository;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.repository.*;
import com.project.hrms.response.AttendanceResponse;
import com.project.hrms.service.AttendanceRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementation cho AttendanceRecordService.
 * - Cập nhật overtime_minutes một cách atomic (pessimistic lock).
 * - Tự tạo attendance record khi không tồn tại (hợp lý cho OT).
 * - Áp dụng cap 480 phút/ngày.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService implements IAttendanceService {

    private final AccountRepository accountRepository;
    private final ShiftAssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceLogRepository logRepository;
    private final OvertimeRequestRepository otRequestRepository;

    // =========================================================================
    // PHẦN 1: LOGIC CŨ (LEGACY) - GIỮ NGUYÊN ĐỂ LEADER TEST
    // =========================================================================
    @Override
    @Transactional
    public int subtractOvertimeMinutes(Long employeeId, LocalDate date, int minutesToSubtract) {

        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateForUpdate(employeeId, date)
                .orElse(null);

        if (record == null) return 0;

        int current = record.getOvertimeMinutes() == null ? 0 : record.getOvertimeMinutes();
        int toSubtract = Math.min(current, Math.max(0, minutesToSubtract));

        record.setOvertimeMinutes(current - toSubtract);
        attendanceRecordRepository.save(record);
        return toSubtract;
    }

    @Override
    public int getTotalWorkMinutes(Long employeeId, LocalDate date) {
        return attendanceRecordRepository
                .findByEmployee_EmployeeIdAndAttendanceDate(employeeId, date)
                .map(r -> r.getTotalWorkMinutes() == null ? 0 : r.getTotalWorkMinutes())
                .orElse(0);
    }


    @Override
    @org.springframework.transaction.annotation.Transactional
    public AttendanceResponse performCheckIn(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Tài khoản không tồn tại"));
        Employee employee = account.getEmployee();

        List<ShiftAssignment> assignments = assignmentRepository
                .findAllByEmployee_EmployeeIdAndAssignmentDate(employee.getEmployeeId(), today);

        if (assignments.isEmpty()) throw new InvalidActionException("Hôm nay không có lịch làm việc!");

        ShiftAssignment assignment = assignments.get(0);
        Shift shift = assignment.getShift();

        List<AttendanceRecord> existingRecords = recordRepository.findByEmployee_EmployeeIdAndAttendanceDate(employee.getEmployeeId(), today);
        if (!existingRecords.isEmpty()) {
            throw new InvalidActionException("Bạn đã Check-in ngày hôm nay rồi (Logic Cũ).");
        }

        AttendanceStatus status = AttendanceStatus.PRESENT;
        long lateMinutes = 0;
        LocalDateTime shiftStartDateTime = today.atTime(shift.getStartTime());
        LocalDateTime graceTime = shiftStartDateTime.plusMinutes(shift.getGraceMinutes());

        if (now.isAfter(graceTime)) {
            status = AttendanceStatus.LATE;
            lateMinutes = ChronoUnit.MINUTES.between(shiftStartDateTime, now);
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setShift(shift);
        record.setAttendanceDate(today);
        record.setCheckInTime(now);
        record.setStatus(status);
        record.setLateMinutes((int) lateMinutes);

        AttendanceRecord savedRecord = recordRepository.save(record);
        return AttendanceResponse.fromEntity(savedRecord);
    }

    @Override
    @Transactional
    public AttendanceResponse performCheckOut(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Account not found"));
        Employee employee = account.getEmployee();

        List<AttendanceRecord> records = recordRepository.findByEmployee_EmployeeIdAndAttendanceDate(employee.getEmployeeId(), today);

        if (records.isEmpty()) {
            throw new InvalidActionException("Chưa Check-in (Logic Cũ).");
        }
        AttendanceRecord record = records.get(0);

        if (record.getCheckOutTime() != null) {
            throw new InvalidActionException("Đã Check-out rồi (Logic Cũ).");
        }

        record.setCheckOutTime(now);

        long workMinutes = ChronoUnit.MINUTES.between(record.getCheckInTime(), now);
        record.setTotalWorkMinutes((int) Math.max(0, workMinutes));

        AttendanceRecord savedRecord = recordRepository.save(record);
        return AttendanceResponse.fromEntity(savedRecord);
    }

    // =========================================================================
    // PHẦN 2: LOGIC MỚI (SMART TAP) - MIN/MAX ALGORITHM
    // =========================================================================

    @Override
    @Transactional
    public AttendanceResponse tapAttendance(String username, AttendanceTapDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        Employee employee = account.getEmployee();

        List<ShiftAssignment> assignments = assignmentRepository.findAllByEmployee_EmployeeIdAndAssignmentDate(employee.getEmployeeId(), today);
        if (assignments.isEmpty()) throw new InvalidActionException("No shift assigned today");

        // 1. [QUAN TRỌNG] Lưu log và FLUSH ngay lập tức để DB có dữ liệu trước khi tính toán
        AttendanceLog newLog = new AttendanceLog();
        newLog.setEmployee(employee);
        newLog.setTime(now);
        newLog.setSource(dto.getSource());
        logRepository.saveAndFlush(newLog); // saveAndFlush để đồng bộ dữ liệu ngay

        // 2. Tính toán lại
        return recalculateDailyAttendance(employee, today, assignments);
    }

    private AttendanceResponse recalculateDailyAttendance(Employee employee, LocalDate date, List<ShiftAssignment> assignments) {
        LocalDateTime startSearch = date.atStartOfDay();
        LocalDateTime endSearch = date.plusDays(1).atTime(12, 0);

        // 1. Lấy Logs và Records hiện có
        List<AttendanceLog> logs = logRepository.findByEmployee_EmployeeIdAndTimeBetweenOrderByTimeAsc(
                employee.getEmployeeId(), startSearch, endSearch);

        List<AttendanceRecord> existingRecords = recordRepository.findByEmployee_EmployeeIdAndAttendanceDate(employee.getEmployeeId(), date);

        AttendanceRecord lastSavedRecord = null;

        // 2. Lấy OT Approved
        List<OvertimeRequest> approvedOts = otRequestRepository.findByEmployee_EmployeeIdAndStatusAndDateBetween(
                employee.getEmployeeId(), RequestStatus.APPROVED, date, date
        );

        // 3. Duyệt qua từng ca để tính toán
        for (ShiftAssignment assignment : assignments) {
            AttendanceRecord record = processSingleShift(assignment, logs, date, approvedOts, existingRecords);
            if (record != null) {
                lastSavedRecord = recordRepository.save(record); // Save lúc này hoạt động như Update
            }
        }

        return lastSavedRecord != null ? AttendanceResponse.fromEntity(lastSavedRecord) : null;
    }

    private AttendanceRecord processSingleShift(ShiftAssignment assignment,
                                                List<AttendanceLog> logs,
                                                LocalDate date,
                                                List<OvertimeRequest> approvedOts,
                                                List<AttendanceRecord> existingRecords) {
        Shift shift = assignment.getShift();

        // 1. Xác định khung giờ CHUẨN
        LocalDateTime shiftStart = date.atTime(shift.getStartTime());
        LocalDateTime shiftEnd = date.atTime(shift.getEndTime());
        if (shift.getEndTime().isBefore(shift.getStartTime())) shiftEnd = shiftEnd.plusDays(1);

        // 2. Tính toán GIỜ KẾT THÚC HỢP LỆ (Valid End Time)
        // Mặc định là hết ca (Shift End)
        LocalDateTime validEndTime = shiftEnd;

        BigDecimal totalOtHours = BigDecimal.ZERO;
        for (OvertimeRequest ot : approvedOts) {
            if (ot.getHours() != null) totalOtHours = totalOtHours.add(ot.getHours());
        }

        // Nếu có OT approved -> Nới rộng giờ hợp lệ ra
        if (totalOtHours.compareTo(BigDecimal.ZERO) > 0) {
            long otMinutes = totalOtHours.multiply(BigDecimal.valueOf(60)).longValue();
            validEndTime = validEndTime.plusMinutes(otMinutes);
        }

        // --- TÌM LOG (Min-Max) ---
        // Window tìm kiếm vẫn để rộng (để bắt log), nhưng tính toán sẽ bị cắt theo validEndTime
        LocalDateTime windowStart = shiftStart.minusHours(4);
        LocalDateTime windowEnd = validEndTime.plusHours(6); // Vẫn tìm rộng ra để bắt log

        AttendanceLog minLog = null;
        AttendanceLog maxLog = null;

        for (AttendanceLog log : logs) {
            if (!log.getTime().isBefore(windowStart) && !log.getTime().isAfter(windowEnd)) {
                if (minLog == null || log.getTime().isBefore(minLog.getTime())) minLog = log;
                if (maxLog == null || log.getTime().isAfter(maxLog.getTime())) maxLog = log;
            }
        }

        if (minLog == null) return null;

        // --- UPDATE RECORD ---
        AttendanceRecord record = existingRecords.stream()
                .filter(r -> r.getShift().getShiftId().equals(shift.getShiftId()))
                .findFirst()
                .orElse(new AttendanceRecord());

        if (record.getAttendanceRecordId() == null) {
            record.setEmployee(assignment.getEmployee());
            record.setShift(shift);
            record.setAttendanceDate(date);
        }

        record.setCheckInTime(minLog.getTime());

        if (maxLog != null && !maxLog.getId().equals(minLog.getId())) {
            record.setCheckOutTime(maxLog.getTime());
        } else {
            record.setCheckOutTime(null);
        }

        // [QUAN TRỌNG] Truyền shiftStart và validEndTime xuống để tính toán chuẩn
        calculateMetricsWithCap(record, shiftStart, validEndTime, shift.getBreakMinutes());

        return record;
    }

    // Hàm tính toán mới: Áp dụng Capping (Chốt chặn)
    private void calculateMetricsWithCap(AttendanceRecord record,
                                         LocalDateTime shiftStart,
                                         LocalDateTime validEndTime,
                                         Integer breakMinutes) {

        // 1. Tính Late (Không đổi)
        if (record.getCheckInTime() != null) {
            long late = ChronoUnit.MINUTES.between(shiftStart, record.getCheckInTime());
            if (late > 0) {
                record.setLateMinutes((int) late);
                record.setStatus(AttendanceStatus.LATE);
            } else {
                record.setLateMinutes(0);
                record.setStatus(AttendanceStatus.PRESENT);
            }
        } else {
            record.setStatus(AttendanceStatus.ABSENT);
            record.setTotalWorkMinutes(0);
            return;
        }

        // 2. Tính Total Work Minutes (CÓ FIX LOGIC)
        if (record.getCheckOutTime() != null) {
            LocalDateTime actualCheckIn = record.getCheckInTime();
            LocalDateTime actualCheckOut = record.getCheckOutTime();

            // A. Chốt chặn giờ vào (Nếu đến sớm quá 8:00 cũng chỉ tính từ 8:00)
            // Tùy policy công ty, thường thì đến sớm không tính công, đến muộn thì tính theo giờ thực
            LocalDateTime effectiveCheckIn = actualCheckIn.isBefore(shiftStart) ? shiftStart : actualCheckIn;

            // B. Chốt chặn giờ ra (KEY FIX): Không được tính quá (ShiftEnd + OT)
            LocalDateTime effectiveCheckOut = actualCheckOut;
            if (actualCheckOut.isAfter(validEndTime)) {
                effectiveCheckOut = validEndTime; // Cắt bớt phần thừa
            }
            // --- THÊM ĐOẠN NÀY ĐỂ DEBUG ---
            System.out.println("DEBUG CHECK:");
            System.out.println("Ca bat dau: " + shiftStart);
            System.out.println("Ca ket thuc (Limit): " + validEndTime);
            System.out.println("Thuc te Vao: " + actualCheckIn + " -> Chot Vao: " + effectiveCheckIn);
            System.out.println("Thuc te Ra: " + actualCheckOut + " -> Chot Ra: " + effectiveCheckOut);
// -----------------------------

            // C. Tính hiệu số
            long workMinutes = 0;
            if (effectiveCheckOut.isAfter(effectiveCheckIn)) {
                workMinutes = ChronoUnit.MINUTES.between(effectiveCheckIn, effectiveCheckOut);
            }

            // D. Trừ Break (Nghỉ trưa)
            // Logic đơn giản: Nếu làm đủ lâu (> 4 tiếng) thì trừ break
            if (breakMinutes != null && breakMinutes > 0) {
                // Chỉ trừ nếu thời gian làm việc bao trùm break (cách đơn giản là check tổng giờ > 4h)
                if (workMinutes >= 240) {
                    workMinutes = Math.max(0, workMinutes - breakMinutes);
                }
            }

            record.setTotalWorkMinutes((int) workMinutes);
        } else {
            // Chưa checkout
            record.setTotalWorkMinutes(0);
        }
    }
}
