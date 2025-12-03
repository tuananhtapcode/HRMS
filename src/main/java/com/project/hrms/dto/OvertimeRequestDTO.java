package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.hrms.model.enums.RequestStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OvertimeRequestDTO {

    private Long id;

    @NotNull(message = "Ngày làm thêm không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Số giờ làm thêm không được để trống")
    @DecimalMin(value = "0.5", message = "Số giờ làm thêm tối thiểu là 0.5")
    private BigDecimal hours;

    private String reason;

    private RequestStatus status;
}