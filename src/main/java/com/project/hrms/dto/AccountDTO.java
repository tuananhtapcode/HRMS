package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AccountDTO {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

//    @JsonProperty("phone_number")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

//    @JsonProperty("role_id")
    @NotNull(message = "Role ID không được để trống")
    private Long roleId;

//    @JsonProperty("employee_id")
    private Long employeeId;

//    // Constructor để tạo DTO từ request
//    public AccountDTO(String username, String password, String email, String phoneNumber) {
//        this.username = username;
//        this.password = password;
//        this.email = email;
//        this.phoneNumber = phoneNumber;
//    }
}
