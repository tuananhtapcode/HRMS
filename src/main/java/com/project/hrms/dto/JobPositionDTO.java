package com.project.hrms.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPositionDTO {

    @NotBlank(message = "Mã chức danh không được để trống")
    @Size(max = 50, message = "Mã chức danh không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "Tên chức danh không được để trống")
    @Size(max = 100, message = "Tên chức danh không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

    @Size(max = 50, message = "Level không được vượt quá 50 ký tự")
    private String level;

    @NotNull(message = "Lương tối thiểu không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Lương tối thiểu phải >= 0")
    private BigDecimal minSalary;

    @NotNull(message = "Lương tối đa không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Lương tối đa phải >= 0")
    private BigDecimal maxSalary;

    // ✅ Custom validation logic
    @AssertTrue(message = "Lương tối đa phải lớn hơn lương tối thiểu")
    public boolean isValidSalaryRange() {
        if (minSalary == null || maxSalary == null) return true; // để tránh lỗi null khi validate từng field
        return maxSalary.compareTo(minSalary) > 0;
    }
}
