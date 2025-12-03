// src/main/java/com/project/hrms/model/OvertimeRequest.java
package com.project.hrms.model;

import com.project.hrms.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "overtime_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long overtimeRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    // Trong SQL là decimal(5,2), map sang BigDecimal
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Pending','Approved','Rejected') DEFAULT 'Pending'")
    private RequestStatus status;
}