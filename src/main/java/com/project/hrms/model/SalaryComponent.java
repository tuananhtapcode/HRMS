package com.project.hrms.model;

import com.project.hrms.model.enums.SalaryComponentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "salary_component",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryComponent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salaryComponentId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    // DB enum('earning','deduction')
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('earning','deduction')")
    private SalaryComponentType type;

    @Lob
    private String description;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isActive = true;

    private BigDecimal amount;

    // 👇 BỔ SUNG TRƯỜNG NÀY ĐỂ LÀM SOFT DELETE
    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;
    // createdAt, updatedAt từ BaseEntity
}
