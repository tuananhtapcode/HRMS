package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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
    private Long employeeId;

    @NotBlank(message = "Họ không được để trống")
    private String fullName;

    private String gender;

    //    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

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

    private String bankAccount;

    private String bankNumber;

    private String address;

    private String status;
}
