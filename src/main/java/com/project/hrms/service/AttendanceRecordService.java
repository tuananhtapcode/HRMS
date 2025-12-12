package com.project.hrms.service;

import com.project.hrms.model.AttendanceRecord;
import com.project.hrms.repository.AttendanceRecordRepository;
import com.project.hrms.service.AttendanceRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    // cap OT tối đa 8 giờ = 480 phút
    private static final int MAX_OT_MINUTES_PER_DAY = 8 * 60;

    @Override
    public int getOvertimeMinutes(Long employeeId, LocalDate date) {
        Optional<AttendanceRecord> opt = attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, date);
        return opt.map(r -> r.getOvertimeMinutes() == null ? 0 : r.getOvertimeMinutes()).orElse(0);
    }

    @Override
    @Transactional
    public int addOvertimeMinutes(Long employeeId, LocalDate date, int minutesToAdd) {
        // lock row để tránh race condition
        Optional<AttendanceRecord> opt = attendanceRecordRepository.findByEmployeeIdAndAttendanceDateForUpdate(employeeId, date);

        AttendanceRecord record = opt.orElseGet(() -> {
            // Nếu chưa có record thì tạo mới cơ bản (không có check-in/out)
            AttendanceRecord r = AttendanceRecord.builder()
                    .employeeId(employeeId)
                    .attendanceDate(date)
                    .status("Present")
                    .totalWorkMinutes(0)
                    .overtimeMinutes(0)
                    .lateMinutes(0)
                    .build();
            return attendanceRecordRepository.save(r);
        });

        int current = record.getOvertimeMinutes() == null ? 0 : record.getOvertimeMinutes();
        int canAdd = Math.max(0, MAX_OT_MINUTES_PER_DAY - current);
        int toAdd = Math.min(canAdd, Math.max(0, minutesToAdd));

        if (toAdd <= 0) return 0;

        record.setOvertimeMinutes(current + toAdd);
        // Optionally: update totalWorkMinutes as well? leave to caller / payroll
        attendanceRecordRepository.save(record);
        return toAdd;
    }

    @Override
    @Transactional
    public int subtractOvertimeMinutes(Long employeeId, LocalDate date, int minutesToSubtract) {
        Optional<AttendanceRecord> opt = attendanceRecordRepository.findByEmployeeIdAndAttendanceDateForUpdate(employeeId, date);
        if (opt.isEmpty()) {
            // Không có record để rollback -> trả 0 (và ghi log là cần kiểm tra)
            return 0;
        }
        AttendanceRecord record = opt.get();
        int current = record.getOvertimeMinutes() == null ? 0 : record.getOvertimeMinutes();
        int toSubtract = Math.min(current, Math.max(0, minutesToSubtract));
        record.setOvertimeMinutes(current - toSubtract);
        attendanceRecordRepository.save(record);
        return toSubtract;
    }

    @Override
    public int getTotalWorkMinutes(Long employeeId, LocalDate date) {
        Optional<AttendanceRecord> opt = attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, date);
        return opt.map(r -> r.getTotalWorkMinutes() == null ? 0 : r.getTotalWorkMinutes()).orElse(0);
    }
}
