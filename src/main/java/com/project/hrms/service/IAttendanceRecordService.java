package com.project.hrms.service;

import com.project.hrms.response.AttendanceResponse;

import java.time.LocalDate;

public interface IAttendanceRecordService
{
    /**
     * Lấy số phút OT đã ghi cho nhân viên trong ngày.
     */
    int getOvertimeMinutes(Long employeeId, LocalDate date);

    /**
     * Cộng overtime minutes (khi đơn OT được approve).
     * Nếu attendance record không tồn tại: tạo mới record cơ bản (không có check-in/out).
     * Trả về số phút đã thực tế cộng (sau cắt cap 480).
     */
    int addOvertimeMinutes(Long employeeId, LocalDate date, int minutesToAdd);

    /**
     * Trừ overtime minutes (khi hủy một đơn đã approve).
     * Trả về số phút đã thực tế trừ.
     */
    int subtractOvertimeMinutes(Long employeeId, LocalDate date, int minutesToSubtract);

    /**
     * Lấy total_work_minutes (nếu cần dùng).
     */
    int getTotalWorkMinutes(Long employeeId, LocalDate date);

    AttendanceResponse performCheckIn(String username);
    AttendanceResponse performCheckOut(String username);
}
