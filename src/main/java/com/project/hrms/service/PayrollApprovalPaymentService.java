package com.project.hrms.service;

import com.project.hrms.dto.PayPayrollPeriodRequestDTO;
import com.project.hrms.dto.PayrollActionResponseDTO;
import com.project.hrms.dto.PayrollPeriodPaymentSummaryDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.Account;
import com.project.hrms.model.PayrollPeriod;
import com.project.hrms.model.enums.PayrollStatus;
import com.project.hrms.model.enums.PaymentMethod;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.PayrollPeriodRepository;
import com.project.hrms.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PayrollApprovalPaymentService {

    private final PayrollRepository payrollRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final AccountRepository accountRepository;

    private Account getCurrentAccount(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tài khoản: " + username));
    }

    // =========================
    // SUMMARY
    // =========================
    @Transactional(readOnly = true)
    public PayrollPeriodPaymentSummaryDTO getSummary(Long periodId) {
        // validate period tồn tại
        payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy kỳ lương periodId=" + periodId));

        return payrollRepository.getPaymentSummaryByPeriod(periodId);
    }

    // =========================
    // APPROVE PERIOD
    // =========================
    @Transactional
    public PayrollActionResponseDTO approvePeriod(Long periodId, String username) {
        PayrollPeriod period = payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy kỳ lương periodId=" + periodId));

        if (Boolean.TRUE.equals(period.getIsClosed())) {
            throw new InvalidParamException("Kỳ lương đã đóng, không thể duyệt.");
        }

        Account acc = getCurrentAccount(username);
        LocalDateTime now = LocalDateTime.now();

        int affected = payrollRepository.bulkApproveByPeriod(
                periodId,
                Set.of(PayrollStatus.CALCULATED, PayrollStatus.DRAFT),
                PayrollStatus.APPROVED,
                now,
                acc.getAccountId(),
                acc.getUsername()
        );

        if (affected == 0) {
            throw new InvalidParamException("Không có payroll nào ở trạng thái CALCULATED/DRAFT để duyệt.");
        }

        return new PayrollActionResponseDTO(periodId, PayrollStatus.APPROVED, affected, now);
    }

    // =========================
    // PAY PERIOD (FULL = paidAmount = totalSalary)
    // =========================
    @Transactional
    public PayrollActionResponseDTO payPeriod(Long periodId, PayPayrollPeriodRequestDTO req, String username) {
        PayrollPeriod period = payrollPeriodRepository.findById(periodId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy kỳ lương periodId=" + periodId));

        if (Boolean.TRUE.equals(period.getIsClosed())) {
            throw new InvalidParamException("Kỳ lương đã đóng, không thể chi trả.");
        }

        PaymentMethod method = (req != null && req.getMethod() != null) ? req.getMethod() : PaymentMethod.BANK_TRANSFER;
        LocalDateTime paidAt = (req != null && req.getPaidAt() != null) ? req.getPaidAt() : LocalDateTime.now();
        String txRef = (req != null) ? req.getTransactionRef() : null;
        String note = (req != null) ? req.getNote() : null;

        Account acc = getCurrentAccount(username);

        int affected = payrollRepository.bulkPayFullByPeriod(
                periodId,
                PayrollStatus.APPROVED,
                PayrollStatus.PAID,
                paidAt,
                acc.getAccountId(),
                acc.getUsername(),
                method,
                txRef,
                note
        );

        if (affected == 0) {
            throw new InvalidParamException("Chưa có payroll nào được APPROVED để chi trả.");
        }

        return new PayrollActionResponseDTO(periodId, PayrollStatus.PAID, affected, paidAt);
    }

    // =========================
    // PAY SINGLE PAYROLL
    // =========================
    @Transactional
    public PayrollActionResponseDTO paySingle(Long payrollId, PayPayrollPeriodRequestDTO req, String username) {
        Account acc = getCurrentAccount(username);

        PaymentMethod method = (req != null && req.getMethod() != null) ? req.getMethod() : PaymentMethod.BANK_TRANSFER;
        LocalDateTime paidAt = (req != null && req.getPaidAt() != null) ? req.getPaidAt() : LocalDateTime.now();
        String txRef = (req != null) ? req.getTransactionRef() : null;
        String note = (req != null) ? req.getNote() : null;

        int affected = payrollRepository.paySingle(
                payrollId,
                PayrollStatus.APPROVED,
                PayrollStatus.PAID,
                paidAt,
                acc.getAccountId(),
                acc.getUsername(),
                method,
                txRef,
                note,
                (req != null) ? req.getOverridePaidAmount() : null
        );

        if (affected == 0) {
            throw new InvalidParamException("Payroll chưa APPROVED hoặc không tồn tại để chi trả.");
        }

        return new PayrollActionResponseDTO(null, PayrollStatus.PAID, affected, paidAt);
    }
}
