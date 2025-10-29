package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true) // Bắt buộc khi kế thừa BaseEntity
@Entity
// Map với tên bảng và (quan trọng) đặt Unique Key
@Table(name = "shift_assignment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"employee_id", "assignment_date"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment extends BaseEntity { // Kế thừa BaseEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shiftAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY) // Map với employee_id
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY) // Map với shift_id
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(nullable = false)
    private LocalDate assignmentDate;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isApproved;

    @Column(length = 255)
    private String note;

    // Các trường createdAt và updatedAt đã có từ BaseEntity
}