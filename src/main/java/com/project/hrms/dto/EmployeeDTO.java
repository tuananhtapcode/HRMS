package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    @NotBlank(message = "Mã nhân viên không được để trống")
//    @Size(max = 20, message = "Mã nhân viên không được vượt quá 20 ký tự")
    @JsonProperty("employee_code")
    private String employeeCode;

    @NotBlank(message = "Họ và tên không được để trống")
    @JsonProperty("full_name")
    private String fullName;

    private String gender;

    //    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("birth_date")
    private LocalDate birthDate;

    //    @Email(message = "Email không hợp lệ")
//    @NotBlank(message = "Email không được để trống")
    private String email;

    //    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    @JsonProperty("phone_number")
    private String phoneNumber;


    @JsonProperty("hire_date")
//    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @JsonProperty("department_id")
    private Long departmentId;

    @JsonProperty("job_position_id")
    private Long jobPositionId;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("bank_account")
    private String bankAccount;

    @JsonProperty("bank_number")
    private String bankNumber;

    private String address;

    private String status;

    @JsonProperty("manager_id")
    private Long managerId;

}
