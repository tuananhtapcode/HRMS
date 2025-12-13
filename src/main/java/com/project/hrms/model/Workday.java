package com.project.hrms.model;

import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.model.enums.DayType;
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
    private Shift shift;

    // --- CÁC TRƯỜNG DỮ LIỆU ---

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double hoursWorked; // Giờ làm việc thực tế

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", length = 50)
    private DayType workType; // NORMAL, WEEKEND...

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", length = 50)
    private AttendanceStatus attendanceStatus; // PRESENT, LATE, LEAVE...

    @Column(name = "hours_overtime", columnDefinition = "DOUBLE DEFAULT 0")
    private Double hoursOvertime; // Giờ OT

    // --- CÁC KHÓA NGOẠI (SNAPSHOT ĐƠN TỪ) ---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id")
    private LeaveRequest leaveRequest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overtime_request_id")
    private OvertimeRequest overtimeRequest;



    // Nếu chưa có entity ShiftChangeRequest thì tạm comment dòng này hoặc tạo entity rỗng
    // @OneToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "shift_change_request_id")
    // private ShiftChangeRequest shiftChangeRequest;
}