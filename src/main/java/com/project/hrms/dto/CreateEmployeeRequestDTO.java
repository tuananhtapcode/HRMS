// src/main/java/com/project/hrms/dto/CreateEmployeeRequestDTO.java
package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEmployeeRequestDTO {
    // Thông tin Employee
    @JsonProperty("employee_code")
    @NotBlank(message = "Mã nhân viên không được để trống")
    private String employeeCode;
    @NotBlank
    private String fullName;
    @Email @NotBlank
    private String email;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("department_id") @NotNull
    private Long departmentId;
    @JsonProperty("job_position_id") @NotNull
    private Long jobPositionId;

    // Thông tin Account
    @JsonProperty("role_id") @NotNull
    private Long roleId;
}