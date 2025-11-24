// src/main/java/com/project/hrms/dto/ShiftRegisterDTO.java
package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ShiftRegisterDTO {

    @JsonProperty("shift_id")
    @NotNull(message = "Bạn phải chọn ca làm việc")
    private Long shiftId;

    @NotNull(message = "Bạn phải chọn ngày đăng ký")
    private LocalDate date;

    private String note; // Ghi chú của nhân viên
}