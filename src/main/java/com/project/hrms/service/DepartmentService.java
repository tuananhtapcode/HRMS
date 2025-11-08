package com.project.hrms.service;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.exception.DataAlreadyExistsException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Department;
import com.project.hrms.repository.DepartmentRepository;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.response.AccountResponse;
import com.project.hrms.response.DepartmentResponse;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DepartmentService implements IDepartmentService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void setupMapper(){
        modelMapper.typeMap(DepartmentDTO.class, Department.class)
                .addMappings(mapper -> mapper.skip(Department::setDepartmentId));
    }

    @Override
    @Transactional
    public Department create(DepartmentDTO newDepartmentDTO) {
        if (departmentRepository.existsByName(newDepartmentDTO.getName())) {
            throw new DataAlreadyExistsException("Department name already exists");
        }
        Department department = modelMapper.map(newDepartmentDTO, Department.class);

        String newCode = generateNextCode();
        department.setCode(newCode);

        if (newDepartmentDTO.getManagerId() != null) {
            employeeRepository.findById(newDepartmentDTO.getManagerId())
                    .ifPresent(department::setManager);
        }
        return departmentRepository.save(department);
    }

    //Sinh code mới dạng D001, D002, ...
    private String generateNextCode() {
        Long nextNumber = 1L;

        // Lấy phòng ban có code lớn nhất
        var lastDepartmentOpt = departmentRepository.findTopByOrderByCodeDesc();
        if (lastDepartmentOpt.isPresent()) {
            String lastCode = lastDepartmentOpt.get().getCode(); // D0001
            try {
                long currentNumber = Long.parseLong(lastCode.substring(1)); // Bỏ chữ D
                nextNumber = currentNumber + 1;
            } catch (NumberFormatException e) {
                // Nếu code bị lỗi format thì bắt đầu lại từ 1
                nextNumber = 1L;
            }
        }

        // format lại D0001, D0002, ...
        return String.format("D%04d", nextNumber);
    }

    @Override
    @Transactional
    public Department update(Long id, DepartmentDTO departmentDTO) {
        Department existingDepartment = getById(id);

        modelMapper.map(departmentDTO, existingDepartment);

        if (departmentDTO.getManagerId() != null) {
            employeeRepository.findById(departmentDTO.getManagerId())
                    .ifPresent(existingDepartment::setManager);
        }

        return departmentRepository.save(existingDepartment);
    }

    @Override
    @Transactional
    public void delete(Long departmentId) {
        Department department = getById(departmentId);
        department.setIsActive(false);
        departmentRepository.save(department);
    }

    @Override
    public Page<DepartmentResponse> getAllPaged(PageRequest pageRequest) {
        return departmentRepository.findAll(pageRequest).map(DepartmentResponse::fromDepartment);
    }
    @Override
    public Department getById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found with id: " + departmentId));
    }

    @Override
    public List<Department> searchByName(String name) {
        return departmentRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Department> findByManager_EmployeeId(Long managerId) {
        return departmentRepository.findByManager_EmployeeId(managerId);
    }
}
