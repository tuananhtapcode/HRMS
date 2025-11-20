package com.project.hrms.model;

import com.project.hrms.model.enums.EmployeeStatus;
import com.project.hrms.model.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @Column(name = "employee_code",nullable = false, unique = true, length = 20)
    @Pattern(regexp = "^[A-Z0-9]{3,20}$", message = "Mã nhân viên phải gồm chữ in hoa và số, độ dài 3-20 ký tự")
    private String employeeCode;

    @Column(nullable = false, name = "full_name", length = 100)
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.OTHER;

    @Column(name = "date_of_birth")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @Column(length = 100)
    @Email(message = "Email không hợp lệ")
    private String email;

    @Column(name = "phone_number", length = 11)
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @Column(name = "hire_date", nullable = false)
    @NotNull(message = "Ngày vào làm không được để trống")
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

    @Column(name = "bank_number", length = 20)
    @Pattern(regexp = "^[0-9]{8,16}$", message = "Số tài khoản ngân hàng không hợp lệ")
    private String bankNumber;

    @Column(length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDate deletedAt;

//    public enum Gender { Male, Female, Other }
//    public enum EmployeeStatus { Active, OnLeave, Resigned, Terminated }

    // ✅ Liên kết 1–1 với Account
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Account account;
}