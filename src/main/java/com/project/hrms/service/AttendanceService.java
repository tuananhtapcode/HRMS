package com.project.hrms.service;

import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidActionException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.repository.*;
import com.project.hrms.response.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AttendanceService implements IAttendanceService {

    private final AccountRepository accountRepository;
    private final ShiftAssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository recordRepository;

    @Override
    @Transactional
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
        if (recordRepository.findByEmployee_EmployeeIdAndAttendanceDate(employee.getEmployeeId(), today).isPresent()) {
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

        AttendanceRecord record = recordRepository
                .findByEmployee_EmployeeIdAndAttendanceDate(employee.getEmployeeId(), today)
                .orElseThrow(() -> new InvalidActionException("Bạn chưa Check-in, không thể Check-out."));

        if (record.getCheckOutTime() != null) {
            throw new InvalidActionException("Bạn đã Check-out ngày hôm nay rồi.");
        }

        record.setCheckOutTime(now);
        long workMinutes = ChronoUnit.MINUTES.between(record.getCheckInTime(), now);
        record.setTotalWorkMinutes((int) workMinutes);

        AttendanceRecord savedRecord = recordRepository.save(record);
        return AttendanceResponse.fromEntity(savedRecord);
    }
}