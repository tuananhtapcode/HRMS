package com.project.hrms.service;

import com.project.hrms.dto.SalaryComponentCreateDTO;
import com.project.hrms.dto.SalaryComponentUpdateDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.SalaryComponent;
import com.project.hrms.model.enums.SalaryComponentType;
import com.project.hrms.repository.SalaryComponentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryComponentService {
    private final SalaryComponentRepository salaryComponentRepository;

    public SalaryComponent create(SalaryComponentCreateDTO dto) {
        if (salaryComponentRepository.existsByCode(dto.getCode())) {
            throw new InvalidParamException("SalaryComponent code đã tồn tại: " + dto.getCode());
        }
        SalaryComponent c = new SalaryComponent();
        c.setCode(dto.getCode());
        c.setName(dto.getName());
        c.setType(dto.getType());
        c.setDescription(dto.getDescription());
        c.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        // 👇 Thêm dòng này
        c.setAmount(dto.getAmount() != null ? dto.getAmount() : java.math.BigDecimal.ZERO);
        return salaryComponentRepository.save(c);
    }

    public List<SalaryComponent> list(SalaryComponentType type) {
        if (type == null) return salaryComponentRepository.findAll();
        return salaryComponentRepository.findByType(type);
    }

    @Transactional
    public SalaryComponent toggleActive(Long id) {
        SalaryComponent c = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy salaryComponentId=" + id));
        c.setIsActive(!Boolean.TRUE.equals(c.getIsActive()));
        return salaryComponentRepository.save(c);
    }

    @Transactional
    public SalaryComponent update(Long id, SalaryComponentUpdateDTO dto) {
        SalaryComponent c = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy salaryComponentId=" + id));
        if (dto.getName() != null) c.setName(dto.getName());
        if (dto.getType() != null) c.setType(dto.getType());
        if (dto.getDescription() != null) c.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) c.setIsActive(dto.getIsActive());
        // 👇 Thêm dòng này để lưu tiền
        if (dto.getAmount() != null) c.setAmount(dto.getAmount());
        return salaryComponentRepository.save(c);
    }
}
