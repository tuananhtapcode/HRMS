package com.project.hrms.service;

import com.project.hrms.dto.PayPayrollPeriodRequestDTO;
import com.project.hrms.dto.PayrollPeriodPaymentSummaryDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.enums.PayrollStatus;
import com.project.hrms.repository.PayrollRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollPaymentService {

    private final PayrollRepository payrollRepository;

    @Transactional
    public PayrollPeriodPaymentSummaryDTO getSummary(Long periodId) {
        PayrollPeriodPaymentSummaryDTO s = payrollRepository.getPaymentSummaryByPeriod(periodId);
        if (s == null) throw new DataNotFoundException("Không có dữ liệu payroll cho periodId=" + periodId);
        return s;
    }

    @Transactional
    public int approvePeriod(Long periodId, Long by, String byName) {
        return payrollRepository.bulkApproveByPeriod(
                periodId,
                List.of(PayrollStatus.CALCULATED),
                PayrollStatus.APPROVED,
                LocalDateTime.now(),
                by,
                byName
        );
    }

    @Transactional
    public int payPeriodFull(Long periodId, PayPayrollPeriodRequestDTO req, Long by, String byName) {
        LocalDateTime paidAt = (req.getPaidAt() != null) ? req.getPaidAt() : LocalDateTime.now();
        return payrollRepository.bulkPayFullByPeriod(
                periodId,
                PayrollStatus.APPROVED,
                PayrollStatus.PAID,
                paidAt,
                by,
                byName,
                req.getMethod(),
                req.getTransactionRef(),
                req.getNote()
        );
    }

    @Transactional
    public int payOne(Long payrollId, PayPayrollPeriodRequestDTO req, Long by, String byName) {
        LocalDateTime paidAt = (req.getPaidAt() != null) ? req.getPaidAt() : LocalDateTime.now();
        return payrollRepository.paySingle(
                payrollId,
                PayrollStatus.APPROVED,
                PayrollStatus.PAID,
                paidAt,
                by,
                byName,
                req.getMethod(),
                req.getTransactionRef(),
                req.getNote(),
                req.getOverridePaidAmount()
        );
    }
}
