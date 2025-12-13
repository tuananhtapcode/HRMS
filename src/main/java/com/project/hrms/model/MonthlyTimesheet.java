package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monthly_timesheet")
@Data // Lombok sẽ tự sinh ra setter setTotalOtHours
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyTimesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private int month;
    private int year;

    // --- CÔNG LÀM VIỆC ---
    private Double standardWorkDays;
    private Double actualWorkDays;

    // --- CÔNG NGHỈ ---
    private Double paidLeaveDays;
    private Double unpaidLeaveDays;
    private Double holidayLeaveDays;

    // --- OT CHI TIẾT ---
    private Double otWeekdayHours;
    private Double otWeekendHours;
    private Double otHolidayHours;

    // [FIX LỖI]: Thêm trường này vào để hết lỗi setTotalOtHours
    private Double totalOtHours;

    // --- PHẠT ---
    private Integer totalLateMinutes;
    private Integer lateCount;

    @Column(length = 20)
    private String status;
}