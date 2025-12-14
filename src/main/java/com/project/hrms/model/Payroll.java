package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payroll")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payroll extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payrollId;

    // employee_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // payroll_period_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    /**
     * LƯU Ý theo schema của bạn: payroll.salary_profile_id FK -> salary_profile.salary_profile_id
     * (tức là trỏ tới 1 dòng salary_profile)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_profile_id", nullable = false)
    private SalaryProfile salaryProfile;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalSalary;
    // --------------------------------------------------------
    // ✅ THÊM 2 TRƯỜNG NÀY ĐỂ KHỚP VỚI REPOSITORY
    // --------------------------------------------------------
    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount; // Lưu tổng thuế của kỳ lương này

    @Column(name = "insurance_amount", precision = 15, scale = 2)
    private BigDecimal insuranceAmount; // Lưu tổng bảo hiểm của kỳ lương này
    // --------------------------------------------------------

    // items
    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollItem> payrollItems = new ArrayList<>();

    // createdAt, updatedAt từ BaseEntity
}
