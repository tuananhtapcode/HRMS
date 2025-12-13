package com.project.hrms.service;

import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidActionException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.AttendanceRecordRepository;
import com.project.hrms.repository.ShiftAssignmentRepository;
import com.project.hrms.response.AttendanceResponse;
import com.project.hrms.service.AttendanceRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Implementation cho AttendanceRecordService.
 * - Cập nhật overtime_minutes một cách atomic (pessimistic lock).
 * - Tự tạo attendance record khi không tồn tại (hợp lý cho OT).
 * - Áp dụng cap 480 phút/ngày.
 */
@Service
@RequiredArgsConstructor
public class AttendanceRecordService implements IAttendanceRecordService{
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AccountRepository accountRepository;
    private final ShiftAssignmentRepository assignmentRepository;

    // cap OT tối đa 8 giờ = 480 phút
    private static final int MAX_OT_MINUTES_PER_DAY = 8 * 60;

    @Override
    public int getOvertimeMinutes(Long employeeId, LocalDate date) {
        return attendanceRecordRepository
                .findByEmployee_EmployeeIdAndAttendanceDate(employeeId, date)
                .map(r -> r.getOvertimeMinutes() == null ? 0 : r.getOvertimeMinutes())
                .orElse(0);
    }

    @Override
    @Transactional
    public int addOvertimeMinutes(Long employeeId, LocalDate date, int minutesToAdd) {

        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateForUpdate(employeeId, date)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy attendance record cho ngày " + date +
                                        ". Không thể cộng OT khi chưa có ca làm việc."
                        )
                );

        int current = record.getOvertimeMinutes() == null ? 0 : record.getOvertimeMinutes();
        int canAdd = Math.max(0, MAX_OT_MINUTES_PER_DAY - current);
        int toAdd = Math.min(canAdd, Math.max(0, minutesToAdd));

        if (toAdd <= 0) return 0;

        record.setOvertimeMinutes(current + toAdd);
        attendanceRecordRepository.save(record);
        return toAdd;
    }

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

        // 1. Tìm Employee
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Tài khoản không tồn tại"));
        Employee employee = account.getEmployee();
        if (employee == null) {
            throw new DataNotFoundException("Tài khoản chưa liên kết với hồ sơ nhân viên");
        }

        // 2. Tìm Ca làm việc
        ShiftAssignment assignment = assignmentRepository
                .findByEmployee_EmployeeIdAndAssignmentDate(employee.getEmployeeId(), today)
                .orElseThrow(() -> new InvalidActionException("Hôm nay bạn không có lịch làm việc!"));

        // 3. Chặn check-in kép
        if (attendanceRecordRepository.findByEmployee_EmployeeIdAndAttendanceDate(employee.getEmployeeId(), today).isPresent()) {
            throw new InvalidActionException("Bạn đã Check-in ngày hôm nay rồi.");
        }

        Shift shift = assignment.getShift();

        // 4. Tính toán đi muộn
        AttendanceStatus status = AttendanceStatus.Present;
        long lateMinutes = 0;

        LocalDateTime shiftStartDateTime = today.atTime(shift.getStartTime());
        LocalDateTime graceTime = shiftStartDateTime.plusMinutes(shift.getGraceMinutes());

        if (now.isAfter(graceTime)) {
            status = AttendanceStatus.Late;
            lateMinutes = ChronoUnit.MINUTES.between(shiftStartDateTime, now);
        }

        // 5. Lưu
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setShift(shift);
        record.setAttendanceDate(today);
        record.setCheckInTime(now);
        record.setStatus(status);
        record.setLateMinutes((int) lateMinutes);

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        return AttendanceResponse.fromEntity(savedRecord);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public AttendanceResponse performCheckOut(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Account not found"));
        Employee employee = account.getEmployee();

        AttendanceRecord record = attendanceRecordRepository
                .findByEmployee_EmployeeIdAndAttendanceDate(employee.getEmployeeId(), today)
                .orElseThrow(() -> new InvalidActionException("Bạn chưa Check-in, không thể Check-out."));

        if (record.getCheckOutTime() != null) {
            throw new InvalidActionException("Bạn đã Check-out ngày hôm nay rồi.");
        }

        record.setCheckOutTime(now);
        long workMinutes = ChronoUnit.MINUTES.between(record.getCheckInTime(), now);
        record.setTotalWorkMinutes((int) workMinutes);

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        return AttendanceResponse.fromEntity(savedRecord);
    }
}
