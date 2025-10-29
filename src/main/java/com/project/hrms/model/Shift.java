package com.project.hrms.model;

import com.project.hrms.model.enums.PayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "shift")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shiftId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer expectedWorkMinutes;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer breakMinutes;

    @Column(columnDefinition = "INT DEFAULT 5")
    private Integer graceMinutes;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean overtimeEligible;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('hourly','fixed') DEFAULT 'hourly'")
    private PayType payType;

    @Column(precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal hourlyRate;

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2) DEFAULT 1.50")
    private BigDecimal overtimeRate;

}