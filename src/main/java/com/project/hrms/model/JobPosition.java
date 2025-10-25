package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

@Entity
@Table(name = "job_position")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPosition extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_position_id")
    private Long jobPositionId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;
    private String level;

    @Column(name = "min_salary")
    @Min(value = 0, message = "Salary must be >= 0")
    private BigDecimal minSalary;

    @Column(name = "max_salary")
    @Min(value = 0, message = "Salary must be >= 0")
    private BigDecimal maxSalary;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
