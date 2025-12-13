package com.project.hrms.model.enums;

public enum AttendanceStatus {
    PRESENT,      // Đi làm bình thường
    LATE,         // Đi muộn
    LEAVE_PAID,   // Nghỉ có phép (Hưởng lương)
    LEAVE_UNPAID, // Nghỉ không lương
    ABSENT,       // Vắng mặt (Không phép)
    BUSINESS_TRIP // Công tác
}
