package com.project.hrms.model;

import com.project.hrms.model.enums.AttendanceStatus;
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

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double paidLeaveDays;    // Công nghỉ phép hưởng lương

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double unauthorizedLeaveDays; // Nghỉ không phép

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double overtimeHours;    // Giờ làm thêm

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer lateMinutes;     // Số phút đi muộn

    @Enumerated(EnumType.STRING)
    private AttendanceStatus finalStatus; // Trạng thái chốt (Present, Late, OnLeave, Absent)
}