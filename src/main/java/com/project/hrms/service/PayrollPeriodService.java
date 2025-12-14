package com.project.hrms.service;

import com.project.hrms.dto.PayrollPeriodCreateDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.PayrollPeriod;
import com.project.hrms.repository.PayrollPeriodRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollPeriodService {
    private final PayrollPeriodRepository payrollPeriodRepository;

    public PayrollPeriod create(PayrollPeriodCreateDTO dto) {
        // tránh trùng kỳ (ngày giao nhau)
        boolean overlap = payrollPeriodRepository
                .existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(dto.getEndDate(), dto.getStartDate());
        if (overlap) throw new InvalidParamException("Kỳ lương bị trùng khoảng thời gian với kỳ khác.");

        PayrollPeriod p = new PayrollPeriod();
        p.setName(dto.getName());
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());
        p.setPaymentDate(dto.getPaymentDate());
        p.setIsClosed(false);
        return payrollPeriodRepository.save(p);
    }

    public List<PayrollPeriod> getAll() {
        return payrollPeriodRepository.findAll();
    }

    @Transactional
    public PayrollPeriod close(Long id) {
        PayrollPeriod p = payrollPeriodRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy payrollPeriodId=" + id));
        p.setIsClosed(true);
        return payrollPeriodRepository.save(p);
    }

    // Phương thức này dùng để lấy kỳ lương theo ID
    public PayrollPeriod getById(Long id) {
        return payrollPeriodRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy kỳ lương với ID=" + id));
    }
}
