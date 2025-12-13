package com.project.hrms.model;

import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.model.enums.WorkType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "workday", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "date"})
})
@Data
public class Workday extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workdayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift; // Ca làm việc thực tế

    // --- CÁC CỘT TỔNG HỢP ---
    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double standardWorkDays; // Công chuẩn (Đi làm)

    // Leave
    @Column
    private Long leaveRequestId;

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double paidLeaveDays;    // Công nghỉ phép hưởng lương

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double unauthorizedLeaveDays; // Nghỉ không phép

    @Column
    private Long overtimeRequestId;

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double overtimeHours;    // Giờ làm thêm

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer lateMinutes;     // Số phút đi muộn

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkType workType = WorkType.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus finalStatus = AttendanceStatus.Present;
}
//Enum WorkType quyết định loại ngày công (bình thường, nghỉ phép, nghỉ ốm…).
//
//Enum AttendanceStatus quyết định trạng thái chấm công.
//
//Khi approve LeaveRequest → tạo Workday theo từng ngày, set workType = LEAVE_PAID / LEAVE_UNPAID / SICK_LEAVE, attendanceStatus = ONLEAVE.
