package com.project.hrms.service;

import com.project.hrms.dto.SalaryProfileUpsertDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.Employee;
import com.project.hrms.model.SalaryComponent;
import com.project.hrms.model.SalaryProfile;
import com.project.hrms.repository.SalaryComponentRepository;
import com.project.hrms.repository.SalaryProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryProfileService {
    private final SalaryProfileRepository salaryProfileRepository;
    private final SalaryComponentRepository salaryComponentRepository;

    public List<SalaryProfile> listByEmployee(Long employeeId) {
        return salaryProfileRepository.findByEmployee_EmployeeId(employeeId);
    }

    @Transactional
    public SalaryProfile upsert(Long employeeId, SalaryProfileUpsertDTO dto) {
        SalaryComponent comp = salaryComponentRepository.findById(dto.getSalaryComponentId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy salaryComponentId=" + dto.getSalaryComponentId()));

        return salaryProfileRepository
                .findByEmployee_EmployeeIdAndSalaryComponent_SalaryComponentId(employeeId, dto.getSalaryComponentId())
                .map(existing -> {
                    existing.setAmount(dto.getAmount());
                    existing.setNote(dto.getNote());
                    return salaryProfileRepository.save(existing);
                })
                .orElseGet(() -> {
                    SalaryProfile sp = new SalaryProfile();
                    Employee empRef = new Employee();
                    empRef.setEmployeeId(employeeId);
                    sp.setEmployee(empRef);
                    sp.setSalaryComponent(comp);
                    sp.setAmount(dto.getAmount());
                    sp.setNote(dto.getNote());
                    return salaryProfileRepository.save(sp);
                });
    }

    @Transactional
    public SalaryProfile update(Long employeeId, Long profileId, SalaryProfileUpsertDTO dto) {
        SalaryProfile sp = salaryProfileRepository.findById(profileId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy salaryProfileId=" + profileId));

        if (!sp.getEmployee().getEmployeeId().equals(employeeId)) {
            throw new InvalidParamException("Profile không thuộc employeeId=" + employeeId);
        }

        SalaryComponent comp = salaryComponentRepository.findById(dto.getSalaryComponentId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy salaryComponentId=" + dto.getSalaryComponentId()));

        sp.setSalaryComponent(comp);
        sp.setAmount(dto.getAmount());
        sp.setNote(dto.getNote());
        return salaryProfileRepository.save(sp);
    }
}
