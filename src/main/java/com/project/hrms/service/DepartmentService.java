package com.project.hrms.service;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.model.Account;
import com.project.hrms.model.Department;
import com.project.hrms.repository.DepartmentRepository;
import com.project.hrms.repository.EmployeeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    public void settupMapper(){
        modelMapper.typeMap(DepartmentDTO.class, Department.class)
                .addMappings(mapper -> mapper.skip(Department::setDepartmentId));
    }

    @Override
    public Department create(DepartmentDTO newDepartmentDTO) {
        Department department = modelMapper.map(newDepartmentDTO, Department.class);

        if (newDepartmentDTO.getManagerId() != null) {
            employeeRepository.findById(newDepartmentDTO.getManagerId())
                    .ifPresent(department::setManager);
        }

        return departmentRepository.save(department);
    }

    @Override
    public Department getById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found with id: " + departmentId));
    }

    @Override
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
    public void deleteById(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Department not found with id: " + departmentId);
        }
        departmentRepository.deleteById(departmentId);
    }

    @Override
    public List<Department> findByManager_EmployeeId(Long managerId) {
        return departmentRepository.findByManager_EmployeeId(managerId);
    }
}
