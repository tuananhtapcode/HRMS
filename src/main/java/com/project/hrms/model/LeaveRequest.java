// src/main/java/com/project/hrms/model/LeaveRequest.java
package com.project.hrms.model;

import com.project.hrms.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "leave_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Người duyệt (Manager/HR)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_approver_id")
    private Account approver;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "leave_type", length = 100, nullable = false)
    private String leaveType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING'")
    private RequestStatus status;
}