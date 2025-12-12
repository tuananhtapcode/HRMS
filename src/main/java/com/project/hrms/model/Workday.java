package com.project.hrms.model;

import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.model.enums.WorkType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "workday")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workdayId;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private Long shiftId; // FK tới shift, có thể null

    @Column(precision = 5, scale = 2)
    private BigDecimal hoursWorked = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkType workType = WorkType.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus attendanceStatus = AttendanceStatus.Present;

    // Leave
    @Column
    private Long leaveRequestId;

    // Overtime
    @Column(precision = 5, scale = 2)
    private BigDecimal hoursOvertime = BigDecimal.ZERO;

    @Column
    private Long overtimeRequestId;

    // Shift change
    @Column
    private Long shiftChangeRequestId;
}
//Enum WorkType quyết định loại ngày công (bình thường, nghỉ phép, nghỉ ốm…).
//
//Enum AttendanceStatus quyết định trạng thái chấm công.
//
//Khi approve LeaveRequest → tạo Workday theo từng ngày, set workType = LEAVE_PAID / LEAVE_UNPAID / SICK_LEAVE, attendanceStatus = ONLEAVE.
