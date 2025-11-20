package com.project.hrms.response;

import java.time.LocalDateTime;
import java.time.LocalDate;

import com.project.hrms.model.Employee;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private LocalDate hireDate;

    // Thông tin tổ chức
    private Long departmentId;
    private String departmentName;
    private Long jobPositionId;
    private String jobPositionName;

    // Ngân hàng
    private String bankName;
    private String bankAccount;
    private String bankNumber;

    // Trạng thái & địa chỉ
    private String address;
    private String status;

    // 🔧 Mapping static method
    public static EmployeeResponse fromEmployee(Employee employee) {
        if (employee == null) return null;

        return EmployeeResponse.builder()
                .employeeId(employee.getEmployeeId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .gender(employee.getGender() != null ? employee.getGender().name() : null)
                .dateOfBirth(employee.getDateOfBirth())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .hireDate(employee.getHireDate())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getDepartmentId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .jobPositionId(employee.getJobPosition() != null ? employee.getJobPosition().getJobPositionId() : null)
                .jobPositionName(employee.getJobPosition() != null ? employee.getJobPosition().getName() : null)
                .bankName(employee.getBankName())
                .bankAccount(employee.getBankAccount())
                .bankNumber(employee.getBankNumber())
                .address(employee.getAddress())
                .status(employee.getStatus() != null ? employee.getStatus().name() : null)
                .build();
    }
}
