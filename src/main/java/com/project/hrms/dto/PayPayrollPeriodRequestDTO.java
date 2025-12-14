package com.project.hrms.dto;

import com.project.hrms.model.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayPayrollPeriodRequestDTO {
    private PaymentMethod method;        // BANK_TRANSFER/CASH/OTHER
    private LocalDateTime paidAt;         // null => now()
    private String transactionRef;        // optional
    private String note;                  // optional
    private BigDecimal overridePaidAmount; // optional (nếu muốn trả khác total)
}
