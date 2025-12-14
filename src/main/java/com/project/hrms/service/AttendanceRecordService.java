package com.project.hrms.service;

import com.project.hrms.dto.AttendanceTapDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidActionException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.model.enums.LeaveType;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.repository.*;
import com.project.hrms.response.AttendanceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceRecordService implements IAttendanceRecordService {

    private final AccountRepository accountRepository;
    private final ShiftAssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceLogRepository logRepository;
    private final OvertimeRequestRepository otRequestRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    private static final int MAX_OT_MINUTES_PER_DAY = 8 * 60; // 8 tiếng OT max

    @Override
    public int getOvertimeMinutes(Long employeeId, LocalDate date) {
        return attendanceRecordRepository
                .findFirstByEmployee_EmployeeIdAndAttendanceDate(employeeId, date)
                .map(r -> r.getOvertimeMinutes() == null ? 0 : r.getOvertimeMinutes())
                .orElse(0);
    }

    @Override
    @Transactional
    public int addOvertimeMinutes(Long employeeId, LocalDate date, int minutesToAdd) {
        // Hàm này dùng cho HR chỉnh sửa thủ công nếu cần
        AttendanceRecord record = attendanceRecordRepository
                .findFirstByEmployee_EmployeeIdAndAttendanceDate(employeeId, date)
                .orElseThrow(() -> new InvalidActionException("Không tìm thấy bảng công ngày " + date));

        int current = record.getOvertimeMinutes() == null ? 0 : record.getOvertimeMinutes();
        int canAdd = Math.max(0, MAX_OT_MINUTES_PER_DAY - current);
        int toAdd = Math.min(canAdd, Math.max(0, minutesToAdd));

        if (toAdd > 0) {
            record.setOvertimeMinutes(current + toAdd);
            attendanceRecordRepository.save(record);
        }
        return toAdd;
    }

    @Override
    @Transactional
    public int subtractOvertimeMinutes(Long employeeId, LocalDate date, int minutesToSubtract) {
        AttendanceRecord record = attendanceRecordRepository
                .findFirstByEmployee_EmployeeIdAndAttendanceDate(employeeId, date)
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
                .findFirstByEmployee_EmployeeIdAndAttendanceDate(employeeId, date)
                .map(r -> r.getTotalWorkMinutes() == null ? 0 : r.getTotalWorkMinutes())
                .orElse(0);
    }

    @Override
    @Transactional
    public AttendanceResponse tapAttendance(String username, AttendanceTapDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        Employee employee = account.getEmployee();

        List<ShiftAssignment> assignments = assignmentRepository
                .findAllByEmployee_EmployeeIdAndAssignmentDate(employee.getEmployeeId(), today);

        if (assignments.isEmpty()) {
            throw new InvalidActionException("Hôm nay nhân viên không có lịch làm việc.");
        }

        // 1. Lưu log thô
        AttendanceLog newLog = new AttendanceLog();
        newLog.setEmployee(employee);
        newLog.setTime(now);
        newLog.setSource(dto.getSource());
        logRepository.saveAndFlush(newLog);

        // 2. Tính toán lại toàn bộ logic trong ngày
        return recalculateDailyAttendance(employee, today, assignments);
    }

    private AttendanceResponse recalculateDailyAttendance(Employee employee, LocalDate date, List<ShiftAssignment> assignments) {
        // Lấy Logs từ 00:00 ngày hiện tại đến 12:00 trưa hôm sau (để cover ca đêm)
        List<AttendanceLog> logs = logRepository.findByEmployee_EmployeeIdAndTimeBetweenOrderByTimeAsc(
                employee.getEmployeeId(), date.atStartOfDay(), date.plusDays(1).atTime(12, 0)
        );

        List<AttendanceRecord> existingRecords = attendanceRecordRepository
                .findByEmployee_EmployeeIdAndAttendanceDate(employee.getEmployeeId(), date);

        List<OvertimeRequest> approvedOts = otRequestRepository.findByEmployeeIdAndStatusAndDateBetween(
                employee.getEmployeeId(), RequestStatus.APPROVED, date, date
        );

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByEmployeeIdAndStatusAndStartDateBetween(
                employee.getEmployeeId(), RequestStatus.APPROVED, date, date
        );

        AttendanceRecord lastSavedRecord = null;
        for (ShiftAssignment assignment : assignments) {
            AttendanceRecord record = processSingleShift(assignment, logs, date, approvedOts, existingRecords, approvedLeaves);
            if (record != null) {
                lastSavedRecord = attendanceRecordRepository.save(record);
            }
        }
        return lastSavedRecord != null ? AttendanceResponse.fromEntity(lastSavedRecord) : null;
    }

    private AttendanceRecord processSingleShift(ShiftAssignment assignment,
                                                List<AttendanceLog> logs,
                                                LocalDate date,
                                                List<OvertimeRequest> approvedOts,
                                                List<AttendanceRecord> existingRecords,
                                                List<LeaveRequest> approvedLeaves) {
        Shift shift = assignment.getShift();

        AttendanceRecord record = existingRecords.stream()
                .filter(r -> r.getShift().getShiftId().equals(shift.getShiftId()))
                .findFirst()
                .orElse(new AttendanceRecord());

        if (record.getAttendanceRecordId() == null) {
            record.setEmployee(assignment.getEmployee());
            record.setShift(shift);
            record.setAttendanceDate(date);
        }

        // --- XỬ LÝ NGHỈ PHÉP (Priority 1) ---
        LeaveRequest leaveForDay = approvedLeaves.stream()
                .filter(l -> !date.isBefore(l.getStartDate()) && !date.isAfter(l.getEndDate()))
                .findFirst()
                .orElse(null);

        if (leaveForDay != null) {
            // Nếu nghỉ phép, set trạng thái và return luôn, không tính log
            if (leaveForDay.getLeaveType() == LeaveType.PAID) {
                record.setStatus(AttendanceStatus.LEAVE_PAID);
                int standardMins = (int) ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime());
                int breakMins = shift.getBreakMinutes() == null ? 0 : shift.getBreakMinutes();
                record.setTotalWorkMinutes(Math.max(0, standardMins - breakMins));
            } else {
                record.setStatus(AttendanceStatus.LEAVE_UNPAID);
                record.setTotalWorkMinutes(0);
            }
            // Reset các thông số thực tế
            record.setCheckInTime(null);
            record.setCheckOutTime(null);
            record.setLateMinutes(0);
            record.setOvertimeMinutes(0);
            return record;
        }

        // --- TÍNH TOÁN KHUNG GIỜ ---
        LocalDateTime shiftStart = date.atTime(shift.getStartTime());
        LocalDateTime shiftEnd = date.atTime(shift.getEndTime());
        if (shift.getEndTime().isBefore(shift.getStartTime())) {
            shiftEnd = shiftEnd.plusDays(1);
        }

        double approvedOtHours = approvedOts.stream()
                .mapToDouble(OvertimeRequest::getTotalHours)
                .sum();
        int approvedOtMinutes = (int) (approvedOtHours * 60);

        LocalDateTime validEndTime = shiftEnd.plusMinutes(approvedOtMinutes);

        // Window quét log: ShiftStart - 4h đến ValidEndTime + 2h
        LocalDateTime windowStart = shiftStart.minusHours(4);
        LocalDateTime windowEnd = validEndTime.plusHours(2);

        // --- LỌC LOGS ---
        AttendanceLog minLog = null;
        AttendanceLog maxLog = null;
        for (AttendanceLog log : logs) {
            if (!log.getTime().isBefore(windowStart) && !log.getTime().isAfter(windowEnd)) {
                if (minLog == null || log.getTime().isBefore(minLog.getTime())) minLog = log;
                if (maxLog == null || log.getTime().isAfter(maxLog.getTime())) maxLog = log;
            }
        }

        // --- TÍNH TOÁN TRẠNG THÁI ---
        if (minLog == null) {
            record.setStatus(AttendanceStatus.ABSENT);
            record.setCheckInTime(null);
            record.setCheckOutTime(null);
            record.setTotalWorkMinutes(0);
            record.setOvertimeMinutes(0);
            record.setLateMinutes(0);
            return record;
        }

        record.setCheckInTime(minLog.getTime());

        long late = ChronoUnit.MINUTES.between(shiftStart, minLog.getTime());
        record.setLateMinutes((int) Math.max(0, late));

        if (late > shift.getGraceMinutes()) {
            record.setStatus(AttendanceStatus.LATE);
        } else {
            record.setStatus(AttendanceStatus.PRESENT);
        }

        // --- TÍNH GIỜ LÀM VÀ OT ---
        if (maxLog != null && !maxLog.getId().equals(minLog.getId())) {
            record.setCheckOutTime(maxLog.getTime());

            // Cắt trần giờ ra
            LocalDateTime effectiveOut = record.getCheckOutTime().isAfter(validEndTime) ? validEndTime : record.getCheckOutTime();

            long rawWorkMinutes = ChronoUnit.MINUTES.between(record.getCheckInTime(), effectiveOut);

            int breakMinutes = (shift.getBreakMinutes() != null) ? shift.getBreakMinutes() : 0;
            // Trừ break nếu làm đủ lâu (ví dụ > 4h)
            if (rawWorkMinutes >= 240 && breakMinutes > 0) {
                rawWorkMinutes = Math.max(0, rawWorkMinutes - breakMinutes);
            }

            record.setTotalWorkMinutes((int) rawWorkMinutes);

            // Tính OT
            long standardShiftDuration = ChronoUnit.MINUTES.between(shiftStart, shiftEnd) - breakMinutes;
            if (rawWorkMinutes > standardShiftDuration) {
                int extra = (int) (rawWorkMinutes - standardShiftDuration);
                // OT được tính = Min(Thời gian làm dư, Thời gian được duyệt)
                record.setOvertimeMinutes(Math.min(extra, approvedOtMinutes));
            } else {
                record.setOvertimeMinutes(0);
            }
        } else {
            // Chưa CheckOut
            record.setCheckOutTime(null);
            record.setTotalWorkMinutes(0);
            record.setOvertimeMinutes(0);
        }

        return record;
    }
}