package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_code",nullable = false, unique = true)
    private String employeeCode;

    @Column(nullable = false, name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.Other;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    private String email;

    @Column(name = "phone_number", length = 11)
    private String phoneNumber;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "job_position_id")
    private JobPosition jobPosition;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_number")
    private String bankNumber;

    private String address;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.Active;

    @Column(name = "deleted_at")
    private LocalDate deletedAt;

    public enum Gender { Male, Female, Other }
    public enum EmployeeStatus { Active, OnLeave, Resigned, Terminated }

    // ✅ Liên kết 1–1 với Account
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Account account;
}