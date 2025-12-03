// src/main/java/com/project/hrms/model/AttendanceRecord.java
package com.project.hrms.model;

import com.project.hrms.model.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_record",
        uniqueConstraints = {
                // Một nhân viên chỉ có 1 bản ghi chấm công 1 ngày
                @UniqueConstraint(columnNames = {"employee_id", "attendance_date"})
        })
@Data
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift; // Ca đã được phân (Kế hoạch)

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column
    private LocalDateTime checkInTime; // Giờ vào (Thực tế)

    @Column
    private LocalDateTime checkOutTime; // Giờ ra (Thực tế)

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Present','Late','Absent','OnLeave') DEFAULT 'Present'")
    private AttendanceStatus status;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer totalWorkMinutes;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer lateMinutes;

    @Column
    private String note;
}