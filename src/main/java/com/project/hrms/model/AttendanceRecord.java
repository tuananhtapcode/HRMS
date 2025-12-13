// src/main/java/com/project/hrms/model/AttendanceRecord.java
package com.project.hrms.model;

import com.project.hrms.model.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_record",
        uniqueConstraints = {
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
    private Shift shift;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column
    private LocalDateTime checkInTime;

    @Column
    private LocalDateTime checkOutTime;

    // --- CẬP NHẬT PHẦN NÀY ---
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PRESENT','LATE','LEAVE_PAID','LEAVE_UNPAID','ABSENT','BUSINESS_TRIP') DEFAULT 'ABSENT'")
    private AttendanceStatus status;
    // --------------------------

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer totalWorkMinutes;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer overtimeMinutes;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer lateMinutes;

    @Column()
    private String note;
}
