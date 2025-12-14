package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payroll_period")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollPeriod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payrollPeriodId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDate paymentDate;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isClosed = false;

    // createdAt, updatedAt từ BaseEntity
}
