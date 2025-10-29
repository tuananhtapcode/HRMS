package com.project.hrms.dto;

import com.project.hrms.model.enums.PayType; // Phải import enum
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class ShiftDTO {

    private Long shiftId; // Dùng cho output

    @NotBlank(message = "Mã ca không được để trống")
    private String code;

    @NotBlank(message = "Tên ca không được để trống")
    private String name;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @NotNull(message = "Số phút làm việc dự kiến không được để trống")
    private Integer expectedWorkMinutes;

    private Integer breakMinutes = 0;

    private Integer graceMinutes = 5;

    // --- CÁC TRƯỜNG BỊ THIẾU ĐÃ THÊM VÀO ---

    // Dùng kiểu Boolean, sẽ map với TINYINT(1)
    private Boolean overtimeEligible = true;

    // Dùng trực tiếp kiểu Enum
    @NotNull(message = "Loại hình trả lương không được để trống")
    private PayType payType = PayType.hourly; // Gán mặc định là 'hourly'

    // Dùng BigDecimal cho các trường tiền tệ
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    private BigDecimal overtimeRate = new BigDecimal("1.5");
}